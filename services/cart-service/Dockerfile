# Stage 1: Build the application
FROM maven:3.9.6 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/cart-service-0.0.1-SNAPSHOT.jar cart-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/cart-service.jar"]
