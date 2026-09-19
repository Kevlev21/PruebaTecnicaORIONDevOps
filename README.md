# Prueba Técnica - DevOps & Platform Engineer (Plataforma ORION)

Este repositorio contiene la solución completa para la prueba técnica de infraestructura, automatización y confiabilidad de la plataforma ORION, estructurada bajo las mejores prácticas de ingeniería de plataforma.

---

## Estructura del Repositorio

* **`orders-service/`** - Microservicio backend desarrollado en Go.
* **`reception-service/`** - Microservicio backend desarrollado en Java (Gradle).
* **`.github/workflows/ci-cd.yaml`** - Pipeline de Integración Continua (CI) en GitHub Actions.
* **`helm/orion-platform/`** - Chart de Helm para el despliegue estandarizado en Kubernetes.
* **`RCA.md`** - Análisis de Causa Raíz (Root Cause Analysis) sobre incidencias críticas en producción.
* **`BACKLOG_REFINED.md`** - Backlog técnico refinado para la estabilización y optimización de la plataforma.

---

## Componentes Implementados

### 1. Confiabilidad y Contenedorización (Docker & Docker Compose)
* Definición de contenedores para los microservicios con **límites estrictos de recursos** (CPU y memoria) para prevenir caídas de nodos por desbordamiento (`OOMKilled`).
* Configuración de entornos aislados para pruebas de integración con bases de datos (`db_test`).

### 2. Automatización y CI/CD (GitHub Actions)
* Pipeline automatizado en `.github/workflows/ci-cd.yaml` configurado para ejecutarse en cada `push` o `pull request` hacia la rama `main`.
* **Validaciones automáticas:**
  * **Go:** Instalación de versión 1.21 y ejecución de pruebas unitarias (`go test -v`).
  * **Java/Gradle:** Configuración de Temurin JDK 17 y compilación automatizada del servicio de recepción (`./gradlew build`).

### 3. Despliegue en Kubernetes (Helm Charts)
* Estructura modular bajo Helm ubicada en `helm/orion-platform/`:
  * `Chart.yaml`: Metadatos del empaquetado de la aplicación.
  * `values.yaml`: Parametrización centralizada de variables (réplicas, recursos, puertos).
  * `templates/deployment.yaml`: Manifiesto de despliegue con control de ciclo de vida.
  * `templates/service.yaml`: Exposición de red interna tipo `ClusterIP`.


