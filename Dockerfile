# FROM eclipse-temurin:17-jre-alpine
# WORKDIR /app

# RUN addgroup -S maahish && adduser -S maahish -G maahish
# USER maahish

# COPY target/maahish-backend-*.jar app.jar

# EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "app.jar"]

# Build stage
# FROM maven:3.9.9-eclipse-temurin-21 AS builder

# WORKDIR /app

# COPY pom.xml .
# COPY src ./src

# RUN mvn clean package -DskipTests

# # Runtime stage
# FROM eclipse-temurin:21-jre-alpine

# WORKDIR /app

# RUN addgroup -S maahish && adduser -S maahish -G maahish

# COPY --from=builder /app/target/maahish-backend-*.jar app.jar

# RUN chown maahish:maahish app.jar

# USER maahish

# EXPOSE 8080

# ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S maahish && adduser -S maahish -G maahish
USER maahish

COPY target/maahish-backend-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]