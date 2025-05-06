# Product Service

Product Service là một microservice trong hệ thống đặt hàng trực tuyến, chịu trách nhiệm quản lý thông tin sản phẩm và tồn kho.

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
Product Service cung cấp thông tin sản phẩm, kiểm tra tồn kho, và cập nhật số lượng tồn kho khi đặt hàng. Nó giao tiếp với MySQL và được sử dụng bởi Order Service và Frontend.

## Tính năng
- Lấy danh sách hoặc chi tiết sản phẩm.
- Kiểm tra số lượng tồn kho.
- Cập nhật số lượng tồn kho.

## Yêu cầu
- **Java 17**
- **Maven**
- **MySQL 8.0**
- **Docker**

## Cài đặt
1. **Clone repository**:
   ```bash
   git clone <repository-url>
   cd online-ordering-system/product-service
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

3. **Tạo bảng `Products`**:
   ```sql
   CREATE TABLE Products (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(255) NOT NULL,
       description TEXT,
       price DECIMAL(10, 2) NOT NULL,
       quantity_in_stock INT NOT NULL
   );
   ```

## Cấu hình
- **File cấu hình** (`src/main/resources/application.yml`):
  ```yaml
  server:
    port: 8082
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
  product-service:
    build: ./product-service
    ports:
      - "8082:8082"
    depends_on:
      - mysql
  ```

## API Endpoints
- `GET /api/products`
  - **Mô tả**: Lấy danh sách sản phẩm.
  - **Response**:
    ```json
    [
      {
        "id": 1,
        "name": "Product A",
        "description": "Description A",
        "price": 10.00,
        "quantityInStock": 100
      }
    ]
    ```
- `GET /api/products/{id}`
  - **Mô tả**: Lấy chi tiết sản phẩm.
  - **Response**:
    ```json
    {
      "id": 1,
      "name": "Product A",
      "description": "Description A",
      "price": 10.00,
      "quantityInStock": 100
    }
    ```
- `GET /api/products/check?productId={id}&quantity={quantity}`
  - **Mô tả**: Kiểm tra tồn kho.
  - **Response**:
    ```json
    {
      "isAvailable": true
    }
    ```
- `PUT /api/products/{id}/updateQuantity?quantity={quantity}`
  - **Mô tả**: Giảm số lượng tồn kho.
  - **Response**: 200 OK.

## Kiểm tra
1. **Thêm dữ liệu mẫu**:
   ```sql
   INSERT INTO Products (name, description, price, quantity_in_stock)
   VALUES ('Product A', 'Description A', 10.00, 100), ('Product B', 'Description B', 20.00, 50);
   ```

2. **Kiểm tra API**:
   - Lấy danh sách sản phẩm:
     ```bash
     curl http://localhost:8082/api/products
     ```
   - Kiểm tra tồn kho:
     ```bash
     curl "http://localhost:8082/api/products/check?productId=1&quantity=5"
     ```
     **Kỳ vọng**: `{"isAvailable": true}`
   - Cập nhật tồn kho:
     ```bash
     curl -X PUT "http://localhost:8082/api/products/1/updateQuantity?quantity=5"
     ```

3. **Kiểm tra log**:
   ```bash
   docker logs product-service
   ```

## Liên kết
- [README chính của dự án](../README.md)
- Các dịch vụ liên quan: [Order Service](../order-service/README.md), [Cart Service](../cart-service/README.md)