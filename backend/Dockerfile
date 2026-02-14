# --- Etapa 1: Build ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiamos el wrapper de maven y el pom para aprovechar la caché de capas
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copiamos el código fuente y compilamos
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Etapa 2: Run ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Creamos un usuario no-root por seguridad (especialmente importante en un POS)
RUN addgroup -S posgroup && adduser -S posuser -G posgroup
USER posuser

# Copiamos solo el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto de Spring Boot
EXPOSE 8080

# Optimizaciones para Java 21 y Virtual Threads
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZScheduler", "-jar", "app.jar"]