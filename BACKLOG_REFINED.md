# Documento de Arquitectura y Backlog Refinado - Orion ITS 

**Autor:** Kevin Gonzalez  
**Rol:** Platform & DevOps Engineer  
**Proyecto:** Evaluación Técnica - Plataforma ITS (Orion)  
**Fecha:** Septiembre 19 2026

## 1. Visión General deL Proyecto

Este documento detalla la descomposición técnica, análisis de riesgos y plan de trabajo para migrar los microservicios de la plataforma ITS (Orders API, RabbitMQ, Orders Worker) desde un entorno local hacia un clúster de Kubernetes seguro, escalable y operable.

---

## 2. Historias de Usuario e Implementación Técnica

### US-01: Contenerización de Microservicios

**Descripción:** Como DevOps Engineer, quiero empaquetar `orders-service` y `orders-worker` en contenedores Docker livianos y seguros para asegurar consistencia entre ambientes.

* **Análisis:**
  * **Dependencias:** Código fuente de los servicios y sus archivos de configuración/dependencias.
  * **Riesgos:** Generar imágenes demasiado grandes o que ejecuten procesos con permisos de root (riesgo de seguridad).
  * **Supuestos:** Las aplicaciones leen variables de entorno para conectarse a RabbitMQ.

* **Refinamiento & Seguridad:**

  * Implementación de *Multi-stage builds* con imágenes base ligeras (`alpine`).
  * Creación de un usuario sin privilegios (`appuser`) dentro del contenedor.
  
* **Descomposición Técnica & Estimación:**
  * `TASK-1.1`: Crear Dockerfile optimizado para `orders-service` (Non-root, Alpine) — **[S / 1h]**
  * `TASK-1.2`: Crear Dockerfile optimizado para `orders-worker` (Non-root, Alpine) — **[S / 1h]**
  * `TASK-1.3`: Crear `docker-compose.yml` local con RabbitMQ y verificaciones de salud (`healthchecks`) — **[M / 2h]**
  * `TASK-1.4`: Pruebas de integración del flujo completo localmente — **[S / 1h]**

---

### US-02: Orquestación y Despliegue en Kubernetes vía Helm
**Descripción:** Como Platform Engineer, quiero empaquetar la arquitectura en un Chart de Helm parametrizado para facilitar el despliegue en entornos K8s.

* **Análisis:**
  * **Dependencias:** Imágenes Docker construidas y disponibles.
  * **Riesgos:** Credenciales escritas en texto plano (*hardcoded*); falta de probes que causen tráfico a pods no preparados.

* **Refinamiento & Seguridad:**
  * Uso de `ConfigMap` para variables públicas y `Secret` para credenciales.
  * Configuración de `SecurityContext` para restringir escalación de privilegios.
  * Definición explicita de `requests` y `limits` de memoria/CPU.
  
* **Descomposición Técnica & Estimación:**
  * `TASK-2.1`: Crear la estructura base del Helm Chart (`Chart.yaml`, `values.yaml`) — **[S / 1h]**
  * `TASK-2.2`: Crear plantillas para `ConfigMap` y `Secret` — **[S / 1h]**
  * `TASK-2.3`: Crear Deployment/Service de `orders-service` con `livenessProbe` y `readinessProbe` — **[M / 2h]**
  * `TASK-2.4`: Crear Deployment de `orders-worker` con límites de recursos adecuados — **[M / 2h]**
  * `TASK-2.5`: Configurar RabbitMQ mediante StatefulSet o subchart con almacenamiento persistente (`PVC`) — **[M / 2h]**

---

### US-03: Automatización de Pipeline CI/CD y Seguridad
**Descripción:** Como DevOps Engineer, quiero un pipeline automatizado para validar código, escanear vulnerabilidades y construir imágenes de manera segura.

* **Análisis:**
  * **Dependencias:** Repositorio en GitHub/GitLab y acceso a un registro de imágenes (GHCR/Docker Hub).
  * **Riesgos:** Subir imágenes con vulnerabilidades críticas a producción.
* **Descomposición Técnica & Estimación:**
  * `TASK-3.1`: Definir flujo de CI/CD (GitHub Actions / GitLab CI) — **[S / 1h]**
  * `TASK-3.2`: Agregar paso de validación y *linting* de manifiestos y Helm Chart — **[S / 1h]**
  * `TASK-3.3`: Integrar escaneo dinámico de vulnerabilidades en imágenes usando **Trivy** — **[M / 2h]**
  * `TASK-3.4`: Automatizar la construcción y publicación de imágenes con tags de commit — **[S / 1h]**

---

### US-04: Diagnóstico y Mitigación de Incidentes (RCA)
**Descripción:** Como Ingeniero de Operaciones, quiero investigar las causas del fallo en `orders-worker` y RabbitMQ para restablecer la operatividad.

* **Descomposición Técnica & Estimación:**
  * `TASK-4.1`: Elaborar documento de análisis de causa raíz `RCA.md` — **[M / 2h]**
  * `TASK-4.2`: Ajustar el límite de memoria de `orders-worker` (aumentar de 128Mi a 256Mi/512Mi) — **[XS / 30m]**
  * `TASK-4.3`: Definir estrategia de autoescalado horizontal (HPA) para los workers — **[S / 1h]**

---

## 3. Matriz de Priorización (MVP)

| Tarea | Descripción | Estimación | Prioridad |
| :--- | :--- | :--- | :--- |
| `TASK-1.1` al `1.4` | Contenerización y Docker Compose local | 5 Horas | **MVP** |
| `TASK-2.1` al `2.5` | Chart de Helm para Kubernetes | 8 Horas | **MVP** |
| `TASK-3.1` al `3.4` | Pipeline de CI/CD con Trivy | 5 Horas | **MVP** |
| `TASK-4.1` al `4.3` | Documentación RCA y ajuste de límites K8s | 3.5 Horas | **MVP** |
| `Opcional` | Integración de Service Mesh / Dashboards dedicados | 12 Horas | *Fase Futura* |

---

## 4. Justificación de Decisiones Técnicas

* **Multi-stage Builds en Docker:** Permite reducir el tamaño de las imágenes al mínimo y eliminar herramientas de compilación que representan riesgos en ejecución.
* **Helm para Kubernetes:** Abstrae la complejidad de los archivos YAML mediante variables (`values.yaml`), permitiendo reusar el mismo despliegue en diferentes ambientes.
* **Seguridad con Trivy:** Permite detectar fallas de seguridad conocidas (CVEs) antes de empaquetar o desplegar cualquier componente.
* **Corrección de Memoria (`OOMKilled`):** El límite previo de 128Mi colapsaba por la carga de 12,500 mensajes acumulados. Subir a 256Mi/512Mi y agregar replicas permite procesar la cola eficientemente.