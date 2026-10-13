# ==============================================================================
# Stage 1: Build payment-service with multi-module Maven parent POM
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy parent POM
COPY pom.xml .

# Strip out sibling microservice modules so payment-service builds independently
RUN sed -i '/account-service/d; /driver-and-vehicle-service/d; /ride-management-service/d' pom.xml

# Copy payment-service module files
COPY payment-service/pom.xml payment-service/
COPY payment-service/src payment-service/src

# Package only payment-service and required parent POM
RUN mvn clean package -DskipTests -pl payment-service -am

# ==============================================================================
# Stage 2: Minimal Runtime Environment
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /workspace/payment-service/target/payment-service-1.0-SNAPSHOT.jar app.jar

# Render default port configuration
ENV SERVER_PORT=10000
ENV PORT=10000
EXPOSE 10000

# Execute Spring Boot with dynamic port mapping for Render
CMD ["sh", "-c", "java -Dserver.port=${PORT:-${SERVER_PORT:-10000}} -jar app.jar"]
