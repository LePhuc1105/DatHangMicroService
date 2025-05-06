# Cart Service

Cart Service là một microservice trong hệ thống đặt hàng trực tuyến, chịu trách nhiệm quản lý giỏ hàng của người dùng.

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
Cart Service cho phép người dùng thêm, cập nhật, và xóa sản phẩm trong giỏ hàng. Nó lưu trữ giỏ hàng trong MySQL và được sử dụng bởi Frontend và Order Service.

## Tính năng
- Thêm sản phẩm vào giỏ hàng.
- Cập nhật số lượng sản phẩm.
- Xóa sản phẩm khỏi giỏ hàng.
- Lấy danh sách sản phẩm trong giỏ hàng.

## Yêu cầu
- **Java 17**
- **Maven**
- **MySQL 8.0**
- **Docker**

## Cài đặt
1. **Clone repository**:
   ```bash
   git clone <repository-url>
   cd online-ordering-system/cart-service
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
     ```

3. **Tạo bảng `CartItems`** (nếu cần thiết, tùy thuộc vào triển khai):
   ```sql
   CREATE TABLE CartItems (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(255) NOT NULL,
       product_id BIGINT NOT NULL,
       quantity INT NOT NULL,
       FOREIGN KEY (product_id) REFERENCES Products(id)
   );
   ```

## Cấu hình
- **File cấu hình** (`src/main/resources/application.yml`):
  ```yaml
  server:
    port: 8084
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
  cart-service:
    build: ./cart-service
    ports:
      - "8084:8084"
    depends_on:
      - mysql
  ```

## API Endpoints
- `GET /api/cart/{username}/items`
  - **Mô tả**: Lấy giỏ hàng của người dùng.
  - **Response**:
    ```json
    [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
    ```
- `POST /api/cart/{username}/items`
  - **Mô tả**: Thêm sản phẩm vào giỏ hàng.
  - **Body**:
    ```json
    {
      "productId": 1,
      "quantity": 2
    }
    ```
  - **Response**: 200 OK.
- `PUT /api/cart/{username}/items/{productId}`
  - **Mô tả**: Cập nhật số lượng sản phẩm.
  - **Body**:
    ```json
    {
      "quantity": 3
    }
    ```
  - **Response**: 200 OK.
- `DELETE /api/cart/{username}/items/{productId}`
  - **Mô tả**: Xóa sản phẩm khỏi giỏ hàng.
  - **Response**: 200 OK.

## Kiểm tra
1. **Thêm dữ liệu mẫu** (yêu cầu `Products` đã có dữ liệu):
   ```sql
   INSERT INTO Products (name, description, price, quantity_in_stock)
   VALUES ('Product A', 'Description A', 10.00, 100);
   ```

2. **Kiểm tra API**:
   - Thêm sản phẩm vào giỏ:
     ```bash
     curl -X POST http://localhost:8084/api/cart/testuser/items -H "Content-Type: application/json" -d '{"productId":1,"quantity":2}'
     ```
   - Lấy giỏ hàng:
     ```bash
     curl http://localhost:8084/api/cart/testuser/items
     ```
     **Kỳ vọng**: `[{"productId":1,"quantity":2}]`
   - Xóa sản phẩm:
     ```bash
     curl -X DELETE http://localhost:8084/api/cart/testuser/items/1
     ```

3. **Kiểm tra log**:
   ```bash
   docker logs cart-service
   ```

## Liên kết
- [README chính của dự án](../README.md)
- Các dịch vụ liên quan: [Product Service](../product-service/README.md), [Order Service](../order-service/README.md)