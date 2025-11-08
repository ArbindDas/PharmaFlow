

# Use Eclipse Temurin JDK 21 (official OpenJDK replacement)
FROM eclipse-temurin:21-jdk

# Optional: If you want smaller image size, you can use:
# FROM eclipse-temurin:21-jdk-jammy

# Set environment variables
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""

# Create app directory
WORKDIR /app

# Copy JAR into the container
COPY target/PharmaFlow-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port (adjust if not 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","./app.jar"]



