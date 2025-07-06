# Stage 1: Build with JDK 24 and Maven
FROM eclipse-temurin:24 AS builder

# Configure build environment
WORKDIR /workspace/app

# Cache Maven dependencies (layer optimization)
COPY mvnw pom.xml ./
COPY .mvn/ .mvn
RUN ./mvnw dependency:go-offline -B

# Build application
COPY src src
RUN ./mvnw package -DskipTests -Dmaven.test.skip=true

# Stage 2: Production image
FROM eclipse-temurin:24-jre

# Security best practices
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app

# Copy the built JAR with fixed name
COPY --from=builder /workspace/app/target/*.jar app.jar

# Runtime configuration
EXPOSE 8080

# Optimized JVM flags for production
ENTRYPOINT ["java", \
            "-XX:+UseZGC", \
            "-Xmx512m", \
            "-Xms256m", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]

# Health check (adjust endpoint as needed)
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Metadata (recommended)
LABEL org.opencontainers.image.title="odm-gateway" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.description="API Gateway Service"