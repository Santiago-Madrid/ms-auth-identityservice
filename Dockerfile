# --- Etapa 1: build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos wd-lib-common directamente a la estructura del repositorio .m2
RUN mkdir -p /root/.m2/repository/com/world-dance/wd-lib-common/0.0.1-SNAPSHOT
COPY libs/wd-lib-common-0.0.1-SNAPSHOT.jar /root/.m2/repository/com/world-dance/wd-lib-common/0.0.1-SNAPSHOT/
COPY libs/wd-lib-common-0.0.1-SNAPSHOT.pom /root/.m2/repository/com/world-dance/wd-lib-common/0.0.1-SNAPSHOT/

# Copiamos el pom.xml del microservicio para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Etapa 2: runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]