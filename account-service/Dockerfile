# ==============================================================================
# Stage 1: Build account-service with multi-module Maven parent POM
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy parent POM
COPY pom.xml .

# Strip out sibling microservice modules so account-service can build independently
RUN sed -i '/driver-and-vehicle-service/d; /ride-management-service/d; /payment-service/d' pom.xml

# Copy account-service POM and source code
COPY account-service/pom.xml account-service/
COPY account-service/src account-service/src

# Package only account-service and required parent POM
RUN mvn clean package -DskipTests -pl account-service -am

# ==============================================================================
# Stage 2: Minimal Runtime Environment
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /workspace/account-service/target/account-service-1.0-SNAPSHOT.jar app.jar

# Render default port configuration
ENV SERVER_PORT=10000
ENV PORT=10000
EXPOSE 10000

# Execute Spring Boot with dynamic port mapping for Render
CMD ["sh", "-c", "java -Dserver.port=${PORT:-${SERVER_PORT:-10000}} -jar app.jar"]
