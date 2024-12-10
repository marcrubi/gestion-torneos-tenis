# Fase de construcción
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package

# Fase de ejecución
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/gestion-torneos-tenis-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
