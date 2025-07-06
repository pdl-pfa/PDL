# Stage 1: Build with JDK 24 and Maven
FROM eclipse-temurin:24 AS builder

# Configure build environment
WORKDIR /workspace/app

# Cache Maven dependencies
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

# Build application
COPY src src
RUN ./mvnw package -DskipTests -Dmaven.test.skip=true

# Stage 2: Production image with JRE 24
FROM eclipse-temurin:24-jre

# Security best practices
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app

# Copy the built JAR with a fixed name
COPY --from=builder /workspace/app/target/*.jar app.jar

# Runtime configuration
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]

# Health check (recommended for microservices)
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8090/actuator/health || exit 1