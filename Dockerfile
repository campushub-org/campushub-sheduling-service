FROM openjdk:17.0.2
VOLUME /tmp
ADD target/scheduling_Service-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]