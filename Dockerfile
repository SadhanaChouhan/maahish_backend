FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S maahish && adduser -S maahish -G maahish
USER maahish

COPY target/maahish-backend-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
