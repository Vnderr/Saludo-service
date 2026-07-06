# --- Etapa 1: Compilación ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# --- Etapa 2: Ejecución ---
FROM eclipse-temurin:21-jre
RUN useradd -m appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]