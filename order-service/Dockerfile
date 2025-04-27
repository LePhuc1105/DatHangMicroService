# Sử dụng image của OpenJDK 17
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy file jar vào container
COPY target/order-service-0.0.1-SNAPSHOT.jar order-service.jar

# Mở cổng 8081 để chạy ứng dụng
EXPOSE 8081

# Chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]
