# Use an official Maven image to build the application
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application skipping tests to speed up the process
RUN mvn clean package -DskipTests

# Use a lightweight JDK image for the runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/tenantmanage-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]
