# Stage 1: Build the application with JDK 24
FROM eclipse-temurin:24 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and the POM file
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Download Maven dependencies
RUN ./mvnw dependency:go-offline

# Copy the application source code
COPY src ./src

# Package the application
RUN ./mvnw package -DskipTests

# Stage 2: Create the final image with JRE 24
FROM eclipse-temurin:24-jre

# Set the working directory
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]