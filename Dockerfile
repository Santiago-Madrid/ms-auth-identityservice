# --- Etapa 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# 1. Compilar e instalar la librería común (wd-lib-common)
COPY wd-lib-common ./wd-lib-common
RUN mvn -f wd-lib-common/pom.xml clean install -DskipTests

# 2. Descargar dependencias del microservicio (Caché de capas)
COPY ms-auth-identityservice/pom.xml ./ms-auth-identityservice/
RUN mvn -f ms-auth-identityservice/pom.xml dependency:go-offline -B

# 3. Copiar código fuente y empaquetar
COPY ms-auth-identityservice/src ./ms-auth-identityservice/src
RUN mvn -f ms-auth-identityservice/pom.xml clean package -DskipTests

# --- Etapa 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/ms-auth-identityservice/target/*.jar app.jar

EXPOSE 9091
ENTRYPOINT ["java", "-jar", "app.jar"]