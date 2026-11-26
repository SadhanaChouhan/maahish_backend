
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven mirror configuration
COPY settings.xml /root/.m2/settings.xml

# Copy project files
COPY pom.xml .
COPY src ./src

# Build Spring Boot application
RUN mvn -B -U clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S maahish && adduser -S maahish -G maahish

COPY --from=build /app/target/*.jar app.jar

USER maahish

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
