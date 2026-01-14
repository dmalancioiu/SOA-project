# Multi-stage build
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Note: Spring Cloud Gateway uses reactive stack (Netty)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
