# Saludo Service — CI/CD en AWS con Kubernetes
**Evaluación Parcial Examen Transversal 

Este proyecto corresponde a la extensión del pipeline de CI/CD desarrollado en la evaluación anterior, incorporando herramientas de **observabilidad, monitoreo y cumplimiento** dentro de un entorno real en AWS con **Amazon EKS (Kubernetes)**.

La aplicación se despliega automáticamente sobre un **clúster Amazon EKS**, almacena sus imágenes en **Amazon ECR**, envía logs a través de **AWS CloudWatch**, y utiliza **health checks** avanzados para garantizar la disponibilidad del microservicio durante su ejecución.

---

## Sobre el Proyecto

La aplicación corresponde a un microservicio desarrollado en **Spring Boot**, que expone los siguientes endpoints:

- `GET /` → Retorna un mensaje de bienvenida con la versión del servicio.
- `GET /saludo?nombre=X` → Retorna un saludo personalizado (por defecto saluda a "Mundo").
- `GET /info` → Entrega información del microservicio.
- `GET /livez` → **Health check de Liveness** - verifica si el servicio está vivo.
- `GET /readyz` → **Health check de Readiness** - verifica si el servicio está listo para recibir tráfico.

**Tecnologías utilizadas:**

- Spring Boot
- Java 21
- Maven
- GitHub Actions
- Docker (Multi-stage)
- Amazon EKS (Kubernetes)
- Amazon ECR
- AWS CloudWatch
- AWS Systems Manager (SSM)
- Grafana (Visualización de métricas)
- Prometheus (Recolección de métricas)
- SonarCloud
- Snyk
- JaCoCo
- Horizontal Pod Autoscaler (HPA)

---

## Arquitectura del Pipeline

Cada vez que se realiza un **push** sobre la rama `main`, GitHub Actions ejecuta automáticamente el pipeline completo.

El flujo considera las siguientes etapas:

###  **FASE 1: Quality Gates (Seguridad, Calidad, Cumplimiento)**

1. **Snyk Security Scan**
   - Analiza las dependencias del proyecto para detectar vulnerabilidades conocidas.
   - Falla si encuentra vulnerabilidades críticas.

2. **SonarCloud Analysis**
   - Analiza la calidad del código utilizando JaCoCo.
   - Detecta bugs, vulnerabilidades, duplicación y deuda técnica.
   - Genera reportes de calidad de código.

3. **Test Coverage (JaCoCo)**
   - Compila el proyecto.
   - Ejecuta las pruebas unitarias.
   - Genera el reporte de cobertura de pruebas.
   - Almacena artefactos para análisis posterior.

4. **Compliance Check**
   - Verifica que exista `README.md`.
   - Valida la presencia del `Dockerfile`.
   - Asegura cumplimiento de estándares del proyecto.

###  **FASE 2: Build**

5. **Build Docker Image & Push a ECR**
   - Construye la imagen Docker utilizando multi-stage build.
   - Autentica con Amazon ECR.
   - Etiqueta la imagen con el SHA del commit (7 dígitos).
   - También etiqueta como `latest`.
   - Realiza push a Amazon ECR (repositorio privado).

###  **FASE 2.5: Acceptance Tests**

6. **Acceptance Tests**
   - Descarga la imagen de ECR.
   - Ejecuta el contenedor localmente en puerto 8080.
   - Valida todos los endpoints:
     - `GET /saludo` → Debe responder 200 OK
     - `GET /info` → Debe responder 200 OK
     - `GET /livez` → Debe responder 200 OK (liveness probe)
     - `GET /readyz` → Debe responder 200 OK (readiness probe)
   - Si algún endpoint falla, el pipeline se detiene.

###  **FASE 3: Deploy en EKS**

7. **Deploy automático en Amazon EKS**
   - Autentica con AWS y actualiza kubeconfig.
   - Aplica manifiestos Kubernetes:
     - `namespace.yaml` → Crea namespace `saludo`
     - `deployment.yaml` → Despliega el microservicio
     - `service.yaml` → Expone el servicio internamente
     - `hpa.yaml` → Configura escalado automático
   - Actualiza la imagen del deployment con la versión compilada.
   - Valida que el rollout se complete exitosamente.
   - Espera máximo 5 minutos para que los pods estén listos.

---

## Health Checks en Kubernetes

El servicio implementa dos tipos de health checks utilizados por Kubernetes:

###  **Readiness Probe** (`/readyz`)
```yaml
readinessProbe:
  httpGet:
    path: /readyz
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 10
  failureThreshold: 3
```
- **Propósito**: Determinar si el pod está listo para recibir tráfico.
- **Frecuencia**: Cada 10 segundos.
- **Retraso inicial**: 5 segundos después de que el contenedor inicie.
- **Tolerancia**: Máximo 3 fallos consecutivos antes de marcar como "No Ready".

Cuando este probe falla, Kubernetes:
- Remueve el pod del load balancer.
- El servicio NO envía tráfico al pod.
- Pero el pod NO se reinicia.

###  **Liveness Probe** (`/livez`)
```yaml
livenessProbe:
  httpGet:
    path: /livez
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 15
  failureThreshold: 3
```
- **Propósito**: Verificar que el pod sigue vivo y funcional.
- **Frecuencia**: Cada 15 segundos.
- **Retraso inicial**: 10 segundos después de que el contenedor inicie.
- **Tolerancia**: Máximo 3 fallos consecutivos antes de reiniciar el pod.

Cuando este probe falla, Kubernetes:
- Mata el pod y lo reinicia automáticamente.
- Se utiliza para recuperarse de bloqueos o deadlocks.

---

## Observabilidad y Monitoreo

Uno de los principales objetivos de esta evaluación fue incorporar mecanismos de observabilidad dentro del proceso de despliegue.

Para ello se utilizó **AWS CloudWatch**, configurando el contenedor Docker para enviar automáticamente todos sus logs mediante el driver oficial `awslogs`.

Esto permite visualizar:

- Logs de inicio de Spring Boot.
- Errores de ejecución.
- Excepciones durante el despliegue.
- Estado del microservicio.
- Disponibilidad del servicio desplegado.
- Health check logs.

La transmisión de logs se realiza de forma automática durante el despliegue, sin necesidad de instalar agentes adicionales dentro del clúster EKS.

---


### Acceso a Grafana

```bash
# Port forward a Grafana
kubectl port-forward -n monitoring svc/grafana 3000:80

# Credenciales por defecto
Username: admin
Password: admin (cambiar en producción)

# URL
http://localhost:3000
```



## Calidad, Seguridad y Cumplimiento

### Calidad

- SonarCloud analiza automáticamente el código en cada ejecución.
- JaCoCo genera el porcentaje de cobertura de pruebas.
- El despliegue solo continúa si las etapas anteriores finalizan correctamente.
- Acceptance tests validan que todos los endpoints responden correctamente.

### Seguridad

- Snyk analiza las dependencias del proyecto buscando vulnerabilidades conocidas.
- Las imágenes Docker son almacenadas de forma privada en Amazon ECR.
- La aplicación corre como usuario no-root dentro del contenedor.
- Los pods tienen límites de recursos configurados para evitar consumo excesivo.
- El namespace `saludo` aísla la aplicación del resto del clúster.

### Cumplimiento

- Todas las credenciales utilizadas por el pipeline se almacenan como **GitHub Secrets**, evitando exponer información sensible.
- El despliegue utiliza credenciales temporales de AWS Academy mediante `AWS_SESSION_TOKEN`.
- El pipeline incluye validaciones automáticas antes de cada fase.
- Los health checks en Kubernetes garantizan que solo pods sanos reciben tráfico.

De esta manera, ante una falla crítica durante las etapas de calidad, seguridad o despliegue, el pipeline se detiene automáticamente evitando desplegar una versión incorrecta del sistema.

---

## Infraestructura Utilizada

La solución fue implementada utilizando servicios de AWS con arquitectura de Kubernetes.

Se utilizaron los siguientes componentes:

- **Amazon EKS**: Clúster Kubernetes administrado donde se ejecutan los microservicios.
- **Amazon ECR**: Almacenamiento privado para imágenes Docker.
- **Amazon EC2**: Nodos del clúster EKS que ejecutan los pods.
- **AWS CloudWatch**: Almacenamiento y visualización de logs del contenedor.
- **AWS Systems Manager (SSM)**: Ejecución remota desde GitHub Actions.
- **Horizontal Pod Autoscaler**: Escalado automático basado en CPU.

---


## Cómo ejecutar el proyecto localmente

### Con Maven
```bash
mvn spring-boot:run
```
La aplicación estará disponible en `http://localhost:8080`

### Construir la imagen Docker
```bash
docker build -t saludo-service .
```

### Ejecutar el contenedor
```bash
docker run -p 8080:8080 saludo-service
```

### Probar los endpoints
```bash
# Saludo personalizado
curl http://localhost:8080/saludo?nombre=Juan

# Información del servicio
curl http://localhost:8080/info

# Health checks
curl http://localhost:8080/livez
curl http://localhost:8080/readyz
```

---



### Despliegue automático
El pipeline CI/CD se encarga automáticamente de:
1. Validar la calidad y seguridad del código.
2. Construir la imagen Docker.
3. Realizar acceptance tests.
4. Desplegar en EKS.
5. Validar que los health checks pasen.

---

## Configuración de Secrets

Para mantener la seguridad del pipeline, todas las credenciales utilizadas durante el proceso CI/CD se almacenan como **GitHub Repository Secrets**, evitando exponer información sensible dentro del repositorio.

Los principales secretos utilizados son:

| Secret | Descripción |
|---------|-------------|
| `AWS_ACCESS_KEY_ID` | Clave de acceso a AWS. |
| `AWS_SECRET_ACCESS_KEY` | Clave secreta asociada a la cuenta de AWS. |
| `AWS_SESSION_TOKEN` | Token temporal requerido por AWS Academy. |
| `AWS_REGION` | Región donde se despliegan los recursos (ej: us-east-1). |
| `EKS_CLUSTER_NAME` | Nombre del clúster EKS. |
| `ECR_REPOSITORY` | Nombre del repositorio en Amazon ECR. |
| `SONAR_TOKEN` | Token de autenticación para SonarCloud. |
| `SONAR_PROJECT_KEY` | Clave del proyecto en SonarCloud. |
| `SONAR_ORGANIZATION` | Organización en SonarCloud. |
| `SNYK_TOKEN` | Token de autenticación para Snyk. |

---

## Flujo Completo del Pipeline

```
Push a main
    ↓
┌─────────────────────────────────────┐
│ FASE 1: Quality Gates              │
├─────────────────────────────────────┤
│ ✓ Snyk Security Scan               │
│ ✓ SonarCloud Analysis              │
│ ✓ JaCoCo Test Coverage             │
│ ✓ Compliance Check                 │
└─────────────────────────────────────┘
    ↓ (Solo si todos pasan)
┌─────────────────────────────────────┐
│ FASE 2: Build                      │
├─────────────────────────────────────┤
│ ✓ Build Docker Image               │
│ ✓ Push a Amazon ECR                │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ FASE 2.5: Acceptance Tests         │
├─────────────────────────────────────┤
│ ✓ Test /saludo endpoint            │
│ ✓ Test /info endpoint              │
│ ✓ Test /livez endpoint             │
│ ✓ Test /readyz endpoint            │
└─────────────────────────────────────┘
    ↓ (Solo si todos pasan)
┌─────────────────────────────────────┐
│ FASE 3: Deploy                     │
├─────────────────────────────────────┤
│ ✓ Apply k8s manifests              │
│ ✓ Update deployment image          │
│ ✓ Wait for rollout                 │
│ ✓ Verify health checks             │
└─────────────────────────────────────┘
    ↓
 Despliegue Exitoso
```

---

## Evidencias del Pipeline

Cada ejecución del pipeline genera evidencia de las distintas etapas del proceso:

- **GitHub Actions**: Historial completo y logs de ejecución.
- **JaCoCo**: Reporte de cobertura de pruebas.
- **SonarCloud**: Análisis de calidad y deuda técnica.
- **Snyk**: Análisis de vulnerabilidades en dependencias.
- **Amazon ECR**: Imagen Docker almacenada con etiqueta y SHA.
- **AWS CloudWatch**: Logs del contenedor en ejecución.
- **Kubernetes Events**: Eventos de despliegue y health checks.
- **Metrics**: CPU y memoria utilizada por los pods.

---

## Uso de IA

Se utilizó la IA como herramienta de apoyo para:

- Resolver errores durante la implementación del pipeline CI/CD.
- Apoyar la redacción y organización de este archivo README.

El diseño del flujo CI/CD, la configuración de la infraestructura en AWS EKS, la implementación del despliegue automático, la integración de Kubernetes fueron desarrollados y validados por el equipo de trabajo.

---
