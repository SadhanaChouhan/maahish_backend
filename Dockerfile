# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S maahish && adduser -S maahish -G maahish

COPY --from=build /app/target/*.jar app.jar

USER maahish

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]