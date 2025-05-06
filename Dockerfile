# Use a lightweight OpenJDK image
FROM openjdk:21-jdk-slim

# Set environment variable
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""

# Add a volume pointing to /tmp
VOLUME /tmp

# Debug: list files to verify build context
RUN mkdir /debug
COPY . /debug

# Add the app jar to the container
COPY target/online-pharmacy-management-0.0.1-SNAPSHOT.jar app.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/app.jar"]
