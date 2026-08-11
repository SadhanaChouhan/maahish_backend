# FROM eclipse-temurin:17-jre-alpine
# WORKDIR /app

# RUN addgroup -S maahish && adduser -S maahish -G maahish
# USER maahish

# COPY target/maahish-backend-*.jar app.jar

# EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "app.jar"]

# Build stage

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -DskipTests

COPY src ./src

RUN mvn clean package -DskipTests


# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S maahish && adduser -S maahish -G maahish

USER maahish

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
