# Stage 1: Build the application
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/order-service-0.0.1-SNAPSHOT.jar order-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]
