# Étape 1 : Build du projet Maven
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .

# Donner les droits d'exécution au script mvnw
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Étape 2 : Exécution du JAR
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]