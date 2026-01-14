# Stage 1: Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/delivery-service-1.0.0.jar delivery-service.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "delivery-service.jar"]
