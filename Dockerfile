
FROM openjdk:21-jdk-slim


ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""


VOLUME /tmp


RUN mkdir /debug
COPY . /debug


COPY target/online-pharmacy-management-0.0.1-SNAPSHOT.jar app.jar


ENTRYPOINT ["java","-jar","/app.jar"]
