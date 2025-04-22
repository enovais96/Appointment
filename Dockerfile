FROM gradle:jdk21 as build

WORKDIR /app
COPY . /app/

RUN gradle build -x test

FROM openjdk:21-slim

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app/app.jar"] 