# ==============================================================================
# Stage 1: Build driver-and-vehicle-service with multi-module Maven parent POM
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy parent POM
COPY pom.xml .

# Strip out sibling microservice modules so driver-and-vehicle-service builds independently
RUN sed -i '/account-service/d; /ride-management-service/d; /payment-service/d' pom.xml

# Copy driver-and-vehicle-service module files
COPY driver-and-vehicle-service/pom.xml driver-and-vehicle-service/
COPY driver-and-vehicle-service/src driver-and-vehicle-service/src

# Package only driver-and-vehicle-service and required parent POM
RUN mvn clean package -DskipTests -pl driver-and-vehicle-service -am

# ==============================================================================
# Stage 2: Minimal Runtime Environment
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /workspace/driver-and-vehicle-service/target/driver-and-vehicle-service-1.0-SNAPSHOT.jar app.jar

# Render default port configuration
ENV SERVER_PORT=10000
ENV PORT=10000
EXPOSE 10000

# Execute Spring Boot with dynamic port mapping for Render
CMD ["sh", "-c", "java -Dserver.port=${PORT:-${SERVER_PORT:-10000}} -jar app.jar"]
