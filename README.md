# Saludo Service — CI/CD en AWS
**Evaluación Parcial N°3**

Este proyecto corresponde a la extensión del pipeline de CI/CD desarrollado en la evaluación anterior, incorporando herramientas de **observabilidad, monitoreo y cumplimiento** dentro de un entorno real en AWS.

La aplicación se despliega automáticamente sobre una **instancia Amazon EC2**, almacena sus imágenes en **Amazon ECR** y envía los logs del contenedor hacia **AWS CloudWatch**, permitiendo monitorear el comportamiento del microservicio durante su ejecución.

---

## Sobre el Proyecto

La aplicación corresponde a un microservicio desarrollado en **Spring Boot**, que expone los siguientes endpoints:

- `GET /` → Retorna un mensaje de bienvenida con la versión del servicio.
- `GET /saludo?nombre=X` → Retorna un saludo personalizado (por defecto saluda a "Mundo").
- `GET /info` → Entrega información del microservicio.
- `GET /actuator/health` → Endpoint utilizado para verificar el estado de salud del servicio.

**Tecnologías utilizadas:**

- Spring Boot
- Java 21
- Maven
- GitHub Actions
- Docker
- Amazon EC2
- Amazon ECR
- AWS CloudWatch
- SonarCloud
- Snyk
- JaCoCo

---

## Arquitectura del Pipeline

Cada vez que se realiza un **push** sobre la rama `main`, GitHub Actions ejecuta automáticamente el pipeline completo.

El flujo considera las siguientes etapas:

1. **Snyk Security Scan**
   - Analiza las dependencias del proyecto para detectar vulnerabilidades conocidas.

2. **Tests + Cobertura JaCoCo**
   - Compila el proyecto.
   - Ejecuta las pruebas unitarias.
   - Genera el reporte de cobertura mediante JaCoCo.

3. **SonarCloud Analysis**
   - Analiza la calidad del código utilizando el reporte generado por JaCoCo.
   - Detecta bugs, vulnerabilidades, duplicación y deuda técnica.

4. **Build Docker Image**
   - Construye la imagen Docker del microservicio.

5. **Push hacia Amazon ECR**
   - Publica automáticamente la imagen generada dentro del repositorio privado de Amazon Elastic Container Registry.

6. **Deploy automático en Amazon EC2**
   - Mediante AWS Systems Manager (SSM) la instancia descarga la nueva imagen desde ECR.
   - Elimina el contenedor anterior.
   - Levanta la nueva versión del servicio.
   - Expone la aplicación en el puerto `8080`.

7. **Health Check**
   - El pipeline valida automáticamente que el endpoint `/actuator/health` responda correctamente.
   - Si el servicio no queda operativo, el despliegue se considera inválido.

---

## Observabilidad y Monitoreo

Uno de los principales objetivos de esta evaluación fue incorporar mecanismos de observabilidad dentro del proceso de despliegue.

Para ello se utilizó **AWS CloudWatch**, configurando el contenedor Docker para enviar automáticamente todos sus logs mediante el driver oficial `awslogs`.

Esto permite visualizar:

- Logs de inicio de Spring Boot.
- Errores de ejecución.
- Excepciones.
- Estado del microservicio.
- Disponibilidad del servicio desplegado.

La transmisión de logs se realiza de forma automática durante el despliegue, sin necesidad de instalar agentes adicionales dentro de la instancia EC2.

---

## Dashboard y Métricas

Como parte de la observabilidad se utilizaron los servicios de AWS para visualizar información relevante del despliegue.

Las principales métricas monitoreadas corresponden a:

- Estado del microservicio.
- Logs de ejecución.
- Errores registrados.
- Tiempo de despliegue.
- Cobertura de pruebas (JaCoCo).
- Calidad del código (SonarCloud).

Estas métricas permiten identificar rápidamente problemas durante el proceso CI/CD y facilitar la toma de decisiones técnicas.

---

## Calidad, Seguridad y Cumplimiento

### Calidad

- SonarCloud analiza automáticamente el código en cada ejecución.
- JaCoCo genera el porcentaje de cobertura de pruebas.
- El despliegue solo continúa si las etapas anteriores finalizan correctamente.

### Seguridad

- Snyk analiza las dependencias del proyecto buscando vulnerabilidades conocidas.
- Las imágenes Docker son almacenadas de forma privada en Amazon ECR.

### Cumplimiento

- Todas las credenciales utilizadas por el pipeline se almacenan como **GitHub Secrets**, evitando exponer información sensible.
- El despliegue utiliza credenciales temporales de AWS Academy mediante `AWS_SESSION_TOKEN`.
- El pipeline incluye una validación automática del endpoint de salud antes de finalizar el despliegue.

De esta manera, ante una falla crítica durante las etapas de calidad o seguridad, el pipeline se detiene automáticamente evitando desplegar una versión incorrecta del sistema.

---

## Infraestructura Utilizada

La solución fue implementada utilizando servicios de AWS.

Se utilizaron los siguientes componentes:

- **Amazon EC2:** servidor donde se ejecuta el microservicio.
- **Amazon ECR:** almacenamiento privado para imágenes Docker.
- **AWS Systems Manager (SSM):** ejecución remota del despliegue desde GitHub Actions.
- **AWS CloudWatch:** almacenamiento y visualización de logs del contenedor.

---

## Configuración de Docker

El proyecto utiliza un **Dockerfile Multi-stage**, dividido en dos etapas:

- **Build Stage**
  - Compila el proyecto utilizando Maven y Java 21.
  - Genera el archivo JAR.

- **Runtime Stage**
  - Utiliza únicamente Eclipse Temurin JRE 21.
  - Reduce considerablemente el tamaño final de la imagen.
  - Evita incluir herramientas de desarrollo en producción.

Durante el despliegue el contenedor se ejecuta configurando el driver de logs de Docker hacia **AWS CloudWatch**, permitiendo centralizar toda la información de ejecución.

---

## Evidencias del Pipeline

Cada ejecución del pipeline genera evidencia de las distintas etapas del proceso:

- Historial completo en GitHub Actions.
- Reporte de cobertura JaCoCo.
- Análisis de SonarCloud.
- Resultado del análisis de Snyk.
- Imagen Docker almacenada en Amazon ECR.
- Logs del contenedor disponibles en AWS CloudWatch.
- Validación automática del endpoint `/actuator/health`.

---

## Cómo ejecutar el proyecto localmente

### Con Maven

```bash
mvn spring-boot:run
```

### Construir la imagen Docker

```bash
docker build -t saludo-service .
```

### Ejecutar el contenedor

```bash
docker run -p 8080:8080 saludo-service
```

## Configuración de Secrets

Para mantener la seguridad del pipeline, todas las credenciales utilizadas durante el proceso CI/CD se almacenan como **GitHub Repository Secrets**, evitando exponer información sensible dentro del repositorio.

Los principales secretos utilizados son:

| Secret | Descripción |
|---------|-------------|
| `AWS_ACCESS_KEY_ID` | Clave de acceso a AWS. |
| `AWS_SECRET_ACCESS_KEY` | Clave secreta asociada a la cuenta de AWS. |
| `AWS_SESSION_TOKEN` | Token temporal requerido por AWS Academy. |
| `AWS_ACCOUNT_ID` | Identificador de la cuenta de AWS utilizado para Amazon ECR. |
| `AWS_REGION` | Región donde se despliegan los recursos . |
| `AWS_INSTANCE_ID` | Identificador de la instancia EC2 donde se realiza el despliegue. |
| `EC2_SSH_KEY` | Llave privada utilizada para la conexión segura con la instancia EC2. |
| `SONAR_TOKEN` | Token de autenticación para SonarCloud. |
| `SNYK_TOKEN` | Token de autenticación para Snyk. |

---

## Uso de IA

Se utilizó la IA como herramienta de apoyo para:

- Resolver errores durante la implementación del pipeline.
- Implementar el envío de logs hacia AWS CloudWatch.
- Apoyar la redacción y organización de este archivo README.

El diseño del flujo CI/CD, la configuración de la infraestructura en AWS, la implementación del despliegue automático y la integración de las herramientas de observabilidad fueron desarrollados y validados por el equipo de trabajo.
