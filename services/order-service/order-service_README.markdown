# Order Service

Order Service là một microservice trong hệ thống đặt hàng trực tuyến, chịu trách nhiệm xử lý đơn hàng và tích hợp với các dịch vụ khác.

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
Order Service quản lý việc tạo và tra cứu đơn hàng. Nó kiểm tra tồn kho, quyền người dùng, thời gian giao hàng, lưu đơn hàng vào MySQL, và gửi yêu cầu gửi email xác nhận đến Notification Service.

## Tính năng
- Tạo đơn hàng với kiểm tra:
  - Tồn kho sản phẩm.
  - Quyền người dùng.
  - Thời gian giao hàng (phải sau 2 ngày và bắt buộc).
- Lấy thông tin đơn hàng.
- Tích hợp với User, Product, Cart, và Notification Service.

## Yêu cầu
- **Java 17**
- **Maven**
- **MySQL 8.0**
- **Docker**
- **Jackson Databind** (cho JSON serialization)

## Cài đặt
1. **Clone repository**:
   ```bash
   git clone <repository-url>
   cd online-ordering-system/order-service
   ```

2. **Cài đặt dependencies**:
   - Đảm bảo `pom.xml` chứa:
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
     <dependency>
         <groupId>com.fasterxml.jackson.core</groupId>
         <artifactId>jackson-databind</artifactId>
         <version>2.15.2</version>
     </dependency>
     ```

3. **Tạo bảng `Orders` và `OrderItems`**:
   ```sql
   CREATE TABLE Orders (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       customer_username VARCHAR(255) NOT NULL,
       total_price DECIMAL(10, 2) NOT NULL,
       delivery_date DATETIME NOT NULL,
       status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (customer_username) REFERENCES Users(username)
   );

   CREATE TABLE OrderItems (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       order_id BIGINT NOT NULL,
       product_id BIGINT NOT NULL,
       quantity INT NOT NULL,
       unit_price DECIMAL(10, 2) NOT NULL,
       FOREIGN KEY (order_id) REFERENCES Orders(id),
       FOREIGN KEY (product_id) REFERENCES Products(id)
   );
   ```

## Cấu hình
- **File cấu hình** (`src/main/resources/application.yml`):
  ```yaml
  server:
    port: 8081
  spring:
    datasource:
      url: jdbc:mysql://mysql:3306/ordering_system
      username: root
      password: password
    jpa:
      hibernate:
        ddl-auto: update
  ```

- **Docker** (trong `docker-compose.yml`):
  ```yaml
  order-service:
    build: ./order-service
    ports:
      - "8081:8081"
    depends_on:
      - mysql
      - user-service
      - product-service
      - cart-service
      - notification-service
  ```

## API Endpoints
- `POST /api/orders`
  - **Mô tả**: Tạo đơn hàng.
  - **Body**:
    ```json
    {
      "customerUsername": "testuser",
      "customerName": "Test User",
      "customerAddress": "123 Street",
      "customerEmail": "test@example.com",
      "customerPhone": "123456789",
      "items": [
        {"productId": 1, "quantity": 2}
      ],
      "deliveryDate": "2025-05-08T00:00:00Z"
    }
    ```
  - **Response**: 200 OK, trả về thông tin đơn hàng.
- `GET /api/orders/{id}`
  - **Mô tả**: Lấy thông tin đơn hàng.
  - **Response**:
    ```json
    {
      "id": 1,
      "customerUsername": "testuser",
      "totalPrice": 20.00,
      "deliveryDate": "2025-05-08T00:00:00",
      "status": "COMPLETED",
      "items": [
        {"productId": 1, "quantity": 2, "unitPrice": 10.00}
      ]
    }
    ```

## Kiểm tra
1. **Thêm dữ liệu mẫu**:
   ```sql
   INSERT INTO Users (username, password, role, is_active, email)
   VALUES ('testuser', 'password', 'CUSTOMER', TRUE, 'test@example.com');
   INSERT INTO Products (name, description, price, quantity_in_stock)
   VALUES ('Product A', 'Description A', 10.00, 100);
   ```

2. **Kiểm tra API**:
   - Tạo đơn hàng:
     ```bash
     curl -X POST http://localhost:8081/api/orders -H "Content-Type: application/json" -d '{"customerUsername":"testuser","customerName":"Test User","customerAddress":"123 Street","customerEmail":"test@example.com","customerPhone":"123456789","items":[{"productId":1,"quantity":2}],"deliveryDate":"2025-05-08T00:00:00Z"}'
     ```
   - Lấy đơn hàng:
     ```bash
     curl http://localhost:8081/api/orders/1
     ```

3. **Kiểm tra log**:
   ```bash
   docker logs order-service
   ```

## Liên kết
- [README chính của dự án](../README.md)
- Các dịch vụ liên quan: [User Service](../user-service/README.md), [Product Service](../product-service/README.md), [Notification Service](../notification-service/README.md)