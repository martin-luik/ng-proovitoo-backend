# syntax=docker/dockerfile:1.6
FROM gradle:8.10.2-jdk21 AS build

ARG NEXUS_URL
ARG NEXUS_USER
ARG NEXUS_PASS

ENV NEXUS_URL=${NEXUS_URL} \
    NEXUS_USER=${NEXUS_USER} \
    NEXUS_PASS=${NEXUS_PASS}

WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon help

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]