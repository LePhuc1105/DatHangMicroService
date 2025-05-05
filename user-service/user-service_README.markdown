# User Service

User Service là một microservice trong hệ thống đặt hàng trực tuyến, chịu trách nhiệm quản lý thông tin và quyền của người dùng.

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
User Service xử lý xác thực người dùng, lưu trữ thông tin khách hàng, và kiểm tra quyền truy cập. Nó giao tiếp với cơ sở dữ liệu MySQL và cung cấp các endpoint REST để các dịch vụ khác (như Order Service) sử dụng.

## Tính năng
- Xác thực người dùng qua `Basic Auth`.
- Lưu và cập nhật thông tin người dùng (tên, địa chỉ, email, số điện thoại).
- Kiểm tra quyền truy cập dựa trên vai trò (`CUSTOMER`, `ADMIN`).

## Yêu cầu
- **Java 17**
- **Maven**
- **MySQL 8.0**
- **Docker** (khuyến nghị để chạy với Docker Compose)

## Cài đặt
1. **Clone repository** (nếu chưa thực hiện từ dự án chính):
   ```bash
   git clone <repository-url>
   cd online-ordering-system/user-service
   ```

2. **Cài đặt dependencies**:
   - Đảm bảo `pom.xml` chứa các dependency sau:
     ```xml
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-web</artifactId>
     </dependency>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-data-jpa</artifactId>
     </dependency>
     <dependency>
         <groupId>com.mysql</groupId>
         <artifactId>mysql-connector-j</artifactId>
         <scope>runtime</scope>
     </dependency>
     ```

3. **Tạo bảng `Users`**:
   ```sql
   CREATE TABLE Users (
       username VARCHAR(255) PRIMARY KEY,
       password VARCHAR(255) NOT NULL,
       role VARCHAR(50) NOT NULL,
       is_active BOOLEAN NOT NULL,
       email VARCHAR(255) NOT NULL
   );
   ```

## Cấu hình
- **File cấu hình** (`src/main/resources/application.yml`):
  ```yaml
  server:
    port: 8083
  spring:
    datasource:
      url: jdbc:mysql://mysql:3306/ordering_system
      username: root
      password: password
    jpa:
      hibernate:
        ddl-auto: update
  ```

- **Docker** (trong `docker-compose.yml` của dự án chính):
  ```yaml
  user-service:
    build: ./user-service
    ports:
      - "8083:8083"
    depends_on:
      - mysql
  ```

## API Endpoints
- `GET /api/users/{username}/permission`
  - **Mô tả**: Kiểm tra quyền truy cập của người dùng.
  - **Headers**: `Authorization: Basic <base64(username:password)>`
  - **Response**: `true` (hợp lệ) hoặc `false` (không hợp lệ).
- `POST /api/users/{username}/info`
  - **Mô tả**: Lưu thông tin người dùng.
  - **Body**:
    ```json
    {
      "name": "string",
      "address": "string",
      "email": "string",
      "phone": "string"
    }
    ```
  - **Response**: 200 OK.
- `GET /api/users/{username}`
  - **Mô tả**: Lấy thông tin người dùng.
  - **Response**:
    ```json
    {
      "username": "string",
      "email": "string",
      "name": "string",
      "address": "string",
      "phone": "string"
    }
    ```

## Kiểm tra
1. **Thêm dữ liệu mẫu**:
   ```sql
   INSERT INTO Users (username, password, role, is_active, email)
   VALUES ('testuser', 'password', 'CUSTOMER', TRUE, 'test@example.com');
   ```

2. **Kiểm tra API**:
   - Kiểm tra quyền:
     ```bash
     curl -H "Authorization: Basic dGVzdHVzZXI6cGFzc3dvcmQ=" http://localhost:8083/api/users/testuser/permission
     ```
     **Kỳ vọng**: `true`
   - Lưu thông tin:
     ```bash
     curl -X POST http://localhost:8083/api/users/testuser/info -H "Content-Type: application/json" -d '{"name":"Test User","address":"123 Street","email":"test@example.com","phone":"123456789"}'
     ```
     **Kỳ vọng**: 200 OK

3. **Kiểm tra log**:
   ```bash
   docker logs user-service
   ```

## Liên kết
- [README chính của dự án](../README.md)
- Các dịch vụ liên quan: [Product Service](../product-service/README.md), [Order Service](../order-service/README.md)