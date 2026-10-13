# ==============================================================================
# Stage 1: Build ride-management-service with multi-module Maven parent POM
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy parent POM
COPY pom.xml .

# Strip out sibling microservice modules so ride-management-service builds independently
RUN sed -i '/account-service/d; /driver-and-vehicle-service/d; /payment-service/d' pom.xml

# Copy ride-management-service module files
COPY ride-management-service/pom.xml ride-management-service/
COPY ride-management-service/src ride-management-service/src

# Package only ride-management-service and required parent POM
RUN mvn clean package -DskipTests -pl ride-management-service -am

# ==============================================================================
# Stage 2: Minimal Runtime Environment
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /workspace/ride-management-service/target/ride-management-service-1.0-SNAPSHOT.jar app.jar

# Render default port configuration
ENV SERVER_PORT=10000
ENV PORT=10000
EXPOSE 10000

# Execute Spring Boot with dynamic port mapping for Render
CMD ["sh", "-c", "java -Dserver.port=${PORT:-${SERVER_PORT:-10000}} -jar app.jar"]
