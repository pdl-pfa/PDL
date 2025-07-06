# Stage 1: Build with JDK 24 and Maven
FROM eclipse-temurin:24 AS builder

# Configure build environment
WORKDIR /workspace/app

# Cache Maven dependencies
COPY mvnw pom.xml ./
COPY .mvn/ .mvn
RUN ./mvnw dependency:go-offline -B

# Build application
COPY src src
RUN ./mvnw package -DskipTests -Dmaven.test.skip=true

# Stage 2: Production image
FROM eclipse-temurin:24-jre

# Security best practices for payment service
RUN addgroup --system payment && \
    adduser --system --disabled-password --ingroup payment payment && \
    mkdir -p /app/payment-data && \
    chown -R payment:payment /app
USER payment:payment

WORKDIR /app

# Copy the built JAR with explicit naming
COPY --from=builder --chown=payment:payment /workspace/app/target/*.jar payment-service.jar

# Payment-specific configurations
ENV JAVA_OPTS="-XX:+UseZGC -Xms256m -Xmx512m -Dfile.encoding=UTF-8"
ENV PAYMENT_DATA_DIR="/app/payment-data"

# Secure default file permissions
RUN umask 0077

# Expose payment service port
EXPOSE 8082

# Entrypoint with payment-specific optimizations
ENTRYPOINT ["java", \
            "-XX:+UseZGC", \
            "-Xmx512m", \
            "-Xms256m", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Djavax.net.ssl.trustStorePassword=changeit", \
            "-jar", "payment-service.jar"]

# Enhanced health check for payment service
HEALTHCHECK --interval=20s --timeout=5s --start-period=30s \
  CMD curl -f http://localhost:8082/actuator/health/payment || exit 1

# Payment-specific labels
LABEL org.opencontainers.image.title="odm-payment" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.description="Payment Processing Service" \
      com.payment.service.type="transaction-processing" \
      com.payment.secure="true"