# Analisis Incidente RabbitMQ Staging

**Fecha del Incidente:** 19 de Septiembre  
**Entorno:** Staging (Kubernetes Cluster)  
**Servicio Afectado:** Pod `rabbitmq-0` / Cola de Mensajería  
**Estado:** Resuelto / Plan de Mitigación en Progreso  
**Autor:** Kevin Gonzalez - DevOps Engineer  

---

## 1. Resumen Incidente

Durante las pruebas de carga en el entorno de Staging, el pod de **RabbitMQ** sufrió múltiples reinicios inesperados debido a la terminación por falta de memoria (**`OOMKilled`** / Exit Code 137). En el momento crítico del incidente, la cola principal de eventos acumuló un pico sostenido de **12,500 mensajes sin procesar (unacknowledged / ready)**.

La causa raíz fue la ausencia de límites explícitos de recursos (`limits.memory`) en el manifiesto de Kubernetes, sumado a una configuración por defecto en el broker de RabbitMQ sin un umbral de memoria (*High Watermark*) adaptado a cgroups de contenedores.

---

## 2. Hipotesis de la Línea de Tiempo del Incidente (Timeline)

Ya que normalmente se utiliza el monitoreo, la forma mas sencilla de averiguar el estado de las acciones es por medio de los logs, en este caso al ser una prueba tecnica y aislada no se suminitro logs, por comodidad y a manera de organizacion se deja a continuacion una linea de tiempo de como pudieron ocurrir los hechos, para fines mas practicos.


* **12:00 AM:** Inicio de pruebas de carga en Staging entre `reception-service` y `orders-service`.
* **12:15 AM:** Incremento acelerado en el número de mensajes en cola (superando los 8,000 mensajes).
* **12:30 AM:** Los consumidores desaceleran el procesamiento debido a contención de recursos en el nodo.
* **12:42 AM:** La memoria consumida por el proceso Erlang/RabbitMQ alcanza el límite físico del nodo/cgroup.
* **12:45 AM:** OOM-Killer del Kernel de Linux liquida el pod `rabbitmq-0`. La cola alcanza los **12,500 mensajes**.
* **12:46 AM:** Kubernetes reinicia el pod (`CrashLoopBackOff` temporal por carga de estado persistente).

---

## 3. Análisis de Causa Raíz (Root Cause Analysis)

### Mecanismo de Falla (Efecto Dominó)

El colapso del servicio no fue un fallo aislado del código de la aplicación, sino un efecto en cadena desencadenado por la falta de gobierno de recursos en la infraestructura:

1. **Ausencia de Restricción en Kubernetes:** Al no definir `resources.limits.memory` en el manifiesto del pod `rabbitmq-0`, el Kernel de Linux y la JVM/Erlang no tenían una cota superior fijada a nivel de contenedor.

2. **Ceguera de Memoria en Erlang:** RabbitMQ asumió que disponía de la RAM total del nodo worker. Al no existir un `High Watermark` adaptado al cgroup, el broker nunca activó el mecanismo de contrapresión (*backpressure*) ni empezó a volcar mensajes a disco para protegerse.
3. **Saturación y Liquidación:** La acumulación de 12,500 mensajes llenó la RAM del host de forma lineal hasta que el Kernel de Linux intervino ejecutando el **OOM-Killer**, destruyendo el proceso del pod (Exit Code 137).

> **Conclusión del diagnóstico:** Si el pod hubiera contado con un límite de recursos en K8s sintonizado con el *Memory High Watermark* del broker, RabbitMQ habría frenado la ingesta de mensajes de `reception-service` y paginado a disco. La cola habría presentado latencia, pero el servicio nunca se habría caído.

### Factores Técnicos Principales:

1. **Kubernetes Configuration:** El pod de RabbitMQ no poseía configuradas las secciones `resources.requests` ni `resources.limits` en su manifiesto de despliegue. Esto permitió que el pod consumiera memoria RAM de forma descontrolada hasta ser destruido por el kernel del nodo worker.

2. **RabbitMQ Memory Watermark Default:** Por defecto, Erlang intenta direccionar memoria basándose en la RAM total del host en lugar de la asignada al contenedor. Al no detectar restricciones de cgroups, el proceso no aplicó contrapresión (*backpressure*) a los productores antes de llegar al colapso.

3. **Desacople en Consumidores:** El microservicio consumidor (`orders-service`) no escaló dinámicamente frente al volumen de 12,500 mensajes, generando una acumulación en RAM de mensajes no confirmados.

---

## 4. Plan de Desastre Inmediato (Short-Term Remediation)

1. **Ajuste de Manifiestos K8s:** Configurar `requests` (1Gi) y `limits` (2Gi) de memoria/CPU explícitos para el pod de RabbitMQ.

2. **Configuración de Memory Watermark:** Inyectar la variable de entorno `RABBITMQ_VM_MEMORY_HIGH_WATERMARK=0.7` (70% del límite del pod) para forzar al broker a volcar mensajes a disco y bloquear la publicación antes de un evento OOM.

3. **Desahogo de Cola:** Implementar un Script/Job de consumo masivo para procesar los 12,500 mensajes de manera controlada y habilitar Dead Letter Queues (DLQ).

---

## 5. Acciones  y Politicas Preventivas a Tener en Cuenta (Long-Term Prevention)

* **HPA / KEDA (Autoscaling):** Implementar **KEDA** (Kubernetes Event-driven Autoscaling) para escalar automáticamente las réplicas de `orders-service` según el número de mensajes pendientes en la cola de RabbitMQ.

* **Monitoreo & Alertas:** Configurar alertas en Prometheus/Grafana para notificar cuando la memoria de RabbitMQ supere el 80% o cuando los mensajes no procesados excedan los 2,000 por más de 5 minutos.

* **Circuit Breakers:** Implementar un patrón de resiliencia en los productores (`reception-service`) para pausar la publicación cuando RabbitMQ active contrapresión.