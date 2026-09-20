# Consumer Service (Java / Spring Boot)

Microservicio en Java desarrollado con **Spring Boot** y **Gradle**. Escucha eventos en la cola de **RabbitMQ** y los almacena en base de datos, exponiendo endpoints de consulta y monitoreo mediante **Spring Boot Actuator**.

## 🚀 Requisitos Previos

* **Java 17+**
* Contenedor de **RabbitMQ** en ejecución (`localhost:5672`).

---

## ▶️ Ejecución del Proyecto

Usa el wrapper de Gradle (`gradlew`) para ejecutar la aplicación localmente:

```bash
# En Linux / macOS
./gradlew bootRun

# En Windows (CMD / PowerShell)
gradlew.bat bootRun
```

## 🧪 Endpoints y Pruebas (cURL)

### 🏥 Health Checks & Actuator Probe Endpoints

Verifica el estado del servicio y sus sondas de disponibilidad/salud (Probes):

**Endpoints health check**

```bash
curl -X GET http://localhost:8080/actuator/health
curl -X GET http://localhost:8080/actuator/health/liveness
curl -X GET http://localhost:8080/actuator/health/readiness
curl -X GET http://localhost:8080/actuator/health/startup
```

## 📩 Consultar Mensajes Procesados (GET /api/v1/messages)

Obtiene la lista de mensajes consumidos desde RabbitMQ y almacenados en la base de datos.

```bash
curl -X GET http://localhost:8080/api/v1/messages
```
