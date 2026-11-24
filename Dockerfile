
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first
COPY pom.xml .

# Download dependencies with retry logic
RUN --mount=type=cache,target=/root/.m2 \
    for i in 1 2 3 4 5; do \
        mvn -B dependency:go-offline && break || \
        (echo "Dependency download failed. Retrying..." && sleep 20); \
    done

# Copy source code
COPY src ./src

# Build Spring Boot application with retry logic
RUN --mount=type=cache,target=/root/.m2 \
    for i in 1 2 3 4 5; do \
        mvn -B clean package -DskipTests && exit 0; \
        echo "Maven build failed. Retrying in 20 seconds..."; \
        sleep 20; \
    done; \
    exit 1

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S maahish && adduser -S maahish -G maahish

# Copy generated JAR
COPY --from=build --chown=maahish:maahish /app/target/*.jar app.jar

# Switch to non-root user
USER maahish

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
