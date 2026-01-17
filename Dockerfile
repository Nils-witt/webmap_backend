
FROM eclipse-temurin:21-jre-alpine

ENV OVERLAYS_PATH='/overlays'
COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]
