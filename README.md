# Saludo Service — Pipeline CI/CD
**Evaluación Parcial N°2**

Este proyecto consiste en un microservicio desarrollado en SpringBoot que utilizamos como base para implementar un pipeline de CI/CD automático mediante **GitHub Actions**, integrando pruebas, análisis de calidad estático y contenedores con Docker.


##  Sobre el Proyecto

La aplicación es un servicio backend que expone los siguientes endpoints:
* `GET /` -> Retorna un mensaje de bienvenida general con la versión del servicio.
* `GET /saludo?nombre=X` -> Retorna un saludo personalizado (por defecto saluda a "Mundo").
* `GET /info` -> Entrega información del microservicio (nombre, versión y descripción del laboratorio).
* `GET /actuator/health` -> Endpoint nativo de monitoreo de salud (clave para verificar el estado de la app en el despliegue).

**Tecnologías ocupadas:** Spring Boot, Java 21 , Maven, JaCoCo, Docker , Docker Compose, Snyk y SonarCloud.


##  Estructura del Pipeline

Cada vez que subimos código a la rama `main`, GitHub Actions ejecuta estos 5 trabajos en secuencia:

1. **Snyk Security Scan:** Escanea el archivo `pom.xml` buscando vulnerabilidades en las librerías. 
2. **Tests + Cobertura JaCoCo:** Compila el proyecto, corre las pruebas unitarias y genera los reportes de cobertura de código mediante JaCoCo.
3. **SonarCloud Analysis:** Toma el reporte de JaCoCo generado en el paso anterior y analiza el código en SonarCloud para buscar bugs, malas prácticas o código duplicado.
4. **Build Docker Image:** Si los pasos anteriores pasan con éxito, se construye la imagen Docker del microservicio.
5. **Deploy Docker Compose:** Construye el archivo JAR final, levanta el contenedor usando Docker Compose y ejecuta un script de *Health check* que consulta el endpoint `/actuator/health` durante un máximo de 2 minutos. Si la app responde correctamente, el despliegue se da por exitoso; al final, se limpian los contenedores para mantener el entorno ordenado.


##  Trazabilidad y Calidad

### Trazabilidad
* **Historial por Commit:** Cada ejecución en la pestaña **Actions** queda amarrada al commit exacto (`github.sha`) que la gatilló, permitiendo rastrear el origen de cualquier fallo.
* **Evidencias (Artefactos):** Al finalizar el pipeline, quedan disponibles para descarga los resultados de las pruebas unitarias, el reporte de cobertura de JaCoCo y los logs del despliegue (`deploy-logs.txt`).

### Calidad y Seguridad
* **Análisis Automático:** SonarCloud evalúa la calidad del código en cada subida, asegurando que no se arrastre deuda técnica en los controladores ni servicios.
* **Validación de Despliegue:** El pipeline incluye un *Smoke Test* en la etapa de despliegue. No asume que la app funciona solo por levantar el contenedor; verifica activamente que el servicio responda de forma correcta en el endpoint de salud antes de cerrar el flujo.

* **Secrets:** Todas las credenciales sensibles (Tokens de Snyk y SonarCloud) se manejan de forma segura a través de los Secrets de GitHub Actions, evitando dejar claves expuestas en el código fuente.

##  Configuración de Docker y Contenedores

El despliegue local y automatizado se gestiona mediante herramientas de contenedores:

* **Dockerfile Multi-stage:** Implementa una estructura en dos etapas. La primera fase usa una imagen de Maven con Java 21 para compilar y empaquetar el JAR (saltándose los tests para agilizar el build ya que fueron validados previamente). La segunda fase genera la imagen final utilizando únicamente el JRE ligero de Eclipse Temurin 21. Esto reduce drásticamente el tamaño de la imagen y evita llevar herramientas de desarrollo innecesarias a producción.
* **Docker Compose:** Orquesta el levantamiento del contenedor (`saludo-app`) exponiendo el servicio a través del puerto `8080`.

**Uso de IA**:
Se utilizó la ia como herramienta de apoyo para la creación del microservicio base, también en la corrección de errores en el ci-cd y en la estructuración de este archivo readme

El diseño de la arquitectura del flujo, la resolución de la lógica en Java de los controladores y servicios, y la ejecución del laboratorio fueron realizados por el equipo de trabajo.


##  Cómo ejecutar en local

**Con Docker Compose:**
```bash
docker compose up --build

**Con Maven:**
mvn spring-boot:run
