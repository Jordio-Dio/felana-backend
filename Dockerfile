# Étape 1 : Build du projet avec l'image officielle Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .

# Compilation du projet Java / Spring Boot
RUN mvn clean package -DskipTests

# Étape 2 : Image d'exécution légère (JRE)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]