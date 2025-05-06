# Notification Service

Notification Service là một microservice trong hệ thống đặt hàng trực tuyến, chịu trách nhiệm gửi email xác nhận đơn hàng.

## Mục lục
- [Mô tả](#mô-tả)
- [Tính năng](#tính-năng)
- [Yêu cầu](#yêu-cầu)
- [Cài đặt](#cài-đặt)
- [Cấu hình](#cấu-hình)
- [API Endpoints](#api-endpoints)
- [Kiểm tra](#kiểm-tra)
- [Liên kết](#liên-kết)

## Mô tả
Notification Service sử dụng **Resend** để gửi email xác nhận khi đơn hàng được tạo thành công. Nó nhận yêu cầu từ Order Service và phân tích danh sách sản phẩm từ JSON.

## Tính năng
- Gửi email xác nhận với thông tin đơn hàng và danh sách sản phẩm.
- Phân tích danh sách sản phẩm từ chuỗi JSON.

## Yêu cầu
- **Java 17**
- **Maven**
- **MySQL 8.0** (tùy chọn, nếu lưu lịch sử email)
- **Docker**
- **Resend API Key**

## Cài đặt
1. **Clone repository**:
   ```bash
   git clone <repository-url>
   cd online-ordering-system/notification-service
   ```

2. **Cài đặt dependencies**:
   - Đảm bảo `pom.xml` chứa:
     ```xml
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-web</artifactId>
     </dependency>
     <dependency>
         <groupId>com.fasterxml.jackson.core</groupId>
         <artifactId>jackson-databind</artifactId>
         <version>2.15.2</version>
     </dependency>
     ```

3. **Cấu hình Resend**:
   - Đăng ký tại [Resend](https://resend.com) để lấy API Key.

## Cấu hình
- **File cấu hình** (`src/main/resources/application.yml`):
  ```yaml
  server:
    port: 8085
  spring:
    datasource:
      url: jdbc:mysql://mysql:3306/ordering_system
      username: root
      password: password
    jpa:
      hibernate:
        ddl-auto: update
  resend:
    api-key: <your-resend-api-key>
  ```

- **Docker** (trong `docker-compose.yml`):
  ```yaml
  notification-service:
    build: ./notification-service
    ports:
      - "8085:8085"
    depends_on:
      - mysql
  ```

## API Endpoints
- `POST /api/notifications/email`
  - **Mô tả**: Gửi email xác nhận đơn hàng.
  - **Body**:
    ```json
    {
      "email": "test@example.com",
      "orderId": 1,
      "status": "COMPLETED",
      "items": "[{\"productId\":1,\"quantity\":2}]",
      "totalPrice": 20.00
    }
    ```
  - **Response**: 200 OK.

## Kiểm tra
1. **Kiểm tra API**:
   - Gửi email xác nhận:
     ```bash
     curl -X POST http://localhost:8085/api/notifications/email -H "Content-Type: application/json" -d '{"email":"test@example.com","orderId":1,"status":"COMPLETED","items":"[{\"productId\":1,\"quantity\":2}]","totalPrice":20.00}'
     ```
     **Kỳ vọng**: Email được gửi đến `test@example.com`.

2. **Kiểm tra log**:
   ```bash
   docker logs notification-service
   ```

3. **Kiểm tra email**:
   - Đăng nhập vào Resend dashboard để xác minh email người nhận.
   - Kiểm tra hộp thư của `test@example.com`.

## Liên kết
- [README chính của dự án](../README.md)
- Các dịch vụ liên quan: [Order Service](../order-service/README.md)