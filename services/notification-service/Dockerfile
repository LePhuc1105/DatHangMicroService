# Sử dụng image của OpenJDK 17
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy file jar của notification-service vào container
COPY target/notification-service-0.0.1-SNAPSHOT.jar notification-service.jar

# Mở cổng 8085 để chạy ứng dụng (cổng có thể thay đổi tùy theo cấu hình của bạn)
EXPOSE 8085

# Chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "/app/notification-service.jar"]
