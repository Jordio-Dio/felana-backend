# Étape 1 : Build du projet avec Maven et JDK 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .

# Compilation du projet Spring Boot
RUN mvn clean package -DskipTests

# Étape 2 : Image d'exécution légère (JRE 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]