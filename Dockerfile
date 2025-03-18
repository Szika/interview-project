FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/demo-project*.jar app.jar

EXPOSE 8080

# Alkalmazás indítása
ENTRYPOINT ["java", "-jar", "app.jar"]