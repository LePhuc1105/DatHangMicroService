# Sử dụng image của OpenJDK 17
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy file jar của cart-service vào container
COPY target/cart-service-0.0.1-SNAPSHOT.jar cart-service.jar

# Mở cổng 8080 để chạy ứng dụng (cổng có thể thay đổi tùy theo cấu hình của bạn)
EXPOSE 8080

# Chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "/app/cart-service.jar"]
