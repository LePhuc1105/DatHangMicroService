# Hệ thống Đặt hàng Trực tuyến

Hệ thống đặt hàng trực tuyến là một ứng dụng microservices cho phép người dùng đăng nhập, xem sản phẩm, thêm sản phẩm vào giỏ hàng, đặt hàng, và nhận email xác nhận. Hệ thống được xây dựng bằng **Spring Boot**, **MySQL**, và giao diện người dùng bằng **HTML/JavaScript** với **Tailwind CSS**.

## Mục lục
- [Tính năng](#tính-năng)
- [Kiến trúc](#kiến-trúc)
- [Yêu cầu](#yêu-cầu)
- [Cài đặt](#cài-đặt)
- [Cấu hình](#cấu-hình)
- [Chạy ứng dụng](#chạy-ứng-dụng)
- [Kiểm tra](#kiểm-tra)
- [API Endpoints](#api-endpoints)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Góp ý](#góp-ý)

## Tính năng
- **Quản lý người dùng**: Đăng nhập, lưu thông tin khách hàng.
- **Quản lý sản phẩm**: Xem danh sách sản phẩm, kiểm tra tồn kho.
- **Giỏ hàng**: Thêm, cập nhật, xóa sản phẩm khỏi giỏ hàng.
- **Đặt hàng**: Tạo đơn hàng với kiểm tra tồn kho và thời gian giao hàng (phải sau 2 ngày).
- **Thông báo**: Gửi email xác nhận đơn hàng qua **Resend**.
- **Giao diện**: Frontend đơn giản với HTML, JavaScript, và Tailwind CSS.

## Kiến trúc
Hệ thống bao gồm các microservices sau, giao tiếp qua REST API:
- **User Service**: Quản lý thông tin và quyền người dùng.
- **Product Service**: Quản lý sản phẩm và tồn kho.
- **Cart Service**: Quản lý giỏ hàng.
- **Order Service**: Xử lý đơn hàng và tích hợp với các dịch vụ khác.
- **Notification Service**: Gửi email xác nhận.
- **Frontend**: Giao diện người dùng chạy trên server tĩnh.

Cơ sở dữ liệu: **MySQL** với các bảng `Users`, `Products`, `Orders`, `OrderItems`.

## Yêu cầu
- **Docker** và **Docker Compose** (để chạy microservices).
- **Java 17** (cho Spring Boot).
- **Node.js** (để chạy server tĩnh cho frontend).
- **MySQL 8.0** (cơ sở dữ liệu).
- **Resend API Key** (cho gửi email).

## Cài đặt
1. **Clone repository**:
   ```bash
   git clone <repository-url>
   cd online-ordering-system
   ```

2. **Cài đặt dependencies**:
   - Đảm bảo `pom.xml` của các dịch vụ có các dependency sau:
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

3. **Cấu hình MySQL**:
   - Tạo database:
     ```sql
     CREATE DATABASE ordering_system;
     ```
   - Tạo các bảng:
     ```sql
     CREATE TABLE Users (
         username VARCHAR(255) PRIMARY KEY,
         password VARCHAR(255) NOT NULL,
         role VARCHAR(50) NOT NULL,
         is_active BOOLEAN NOT NULL,
         email VARCHAR(255) NOT NULL
     );

     CREATE TABLE Products (
         id BIGINT AUTO_INCREMENT PRIMARY KEY,
         name VARCHAR(255) NOT NULL,
         description TEXT,
         price DECIMAL(10, 2) NOT NULL,
         quantity_in_stock INT NOT NULL
     );

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

4. **Cài đặt Node.js** (cho frontend):
   ```bash
   npm install -g http-server
   ```

## Cấu hình
1. **File cấu hình (`application.yml`)**:
   - **User Service** (`user-service/src/main/resources/application.yml`):
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
   - **Product Service** (`product-service/src/main/resources/application.yml`):
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
   - **Cart Service** (`cart-service/src/main/resources/application.yml`):
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
   - **Order Service** (`order-service/src/main/resources/application.yml`):
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
   - **Notification Service** (`notification-service/src/main/resources/application.yml`):
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

2. **Docker Compose (`docker-compose.yml`)**:
   ```yaml
   version: '3.8'
   services:
     mysql:
       image: mysql:8.0
       environment:
         MYSQL_ROOT_PASSWORD: password
         MYSQL_DATABASE: ordering_system
       ports:
         - "3306:3306"
       volumes:
         - mysql-data:/var/lib/mysql
     user-service:
       build: ./user-service
       ports:
         - "8083:8083"
       depends_on:
         - mysql
     product-service:
       build: ./product-service
       ports:
         - "8082:8082"
       depends_on:
         - mysql
     cart-service:
       build: ./cart-service
       ports:
         - "8084:8084"
       depends_on:
         - mysql
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
     notification-service:
       build: ./notification-service
       ports:
         - "8085:8085"
       depends_on:
         - mysql
   volumes:
     mysql-data:
   ```

3. **Frontend**:
   - Đặt file `index.html` trong thư mục `frontend/`.

## Chạy ứng dụng
1. **Chạy backend**:
   ```bash
   docker-compose up --build
   ```

2. **Chạy frontend**:
   ```bash
   cd frontend
   npx http-server -p 3000
   ```

3. **Truy cập**:
   - Frontend: `http://localhost:3000`
   - API Documentation (nếu có Swagger): `http://localhost:<port>/swagger-ui.html`

## Kiểm tra
1. **Thêm dữ liệu mẫu**:
   ```sql
   INSERT INTO Users (username, password, role, is_active, email) 
   VALUES ('testuser', 'password', 'CUSTOMER', TRUE, 'test@example.com');

   INSERT INTO Products (name, description, price, quantity_in_stock) 
   VALUES ('Product A', 'Description A', 10.00, 100), ('Product B', 'Description B', 20.00, 50);
   ```

2. **Kiểm tra chức năng**:
   - Đăng nhập với `testuser:password`.
   - Thêm sản phẩm vào giỏ hàng.
   - Đặt hàng với ngày giao hàng sau 2 ngày (ví dụ, 2025-05-08 nếu hôm nay là 2025-05-05).
   - Kiểm tra email xác nhận (đảm bảo email được xác minh trong Resend dashboard).
   - Kiểm tra log:
     ```bash
     docker logs order-service
     docker logs notification-service
     ```
   - Kiểm tra bảng `Orders` và `OrderItems`:
     ```sql
     SELECT * FROM Orders;
     SELECT * FROM OrderItems;
     ```

3. **Kiểm tra lỗi**:
   - Đặt hàng mà không chọn ngày giao hàng → Lỗi: "Thời gian giao hàng là bắt buộc".
   - Đặt hàng với ngày giao hàng trước 2 ngày → Lỗi: "Thời gian giao hàng phải ít nhất 2 ngày sau thời điểm hiện tại".
   - Đặt hàng với số lượng vượt quá tồn kho → Lỗi: "Sản phẩm không đủ số lượng trong kho".

## API Endpoints
- **User Service** (`http://localhost:8083`):
  - `GET /api/users/{username}/permission`: Kiểm tra quyền người dùng.
  - `POST /api/users/{username}/info`: Lưu thông tin người dùng.
- **Product Service** (`http://localhost:8082`):
  - `GET /api/products`: Lấy danh sách sản phẩm.
  - `GET /api/products/{id}`: Lấy thông tin sản phẩm.
  - `GET /api/products/check`: Kiểm tra tồn kho.
  - `PUT /api/products/{id}/updateQuantity`: Cập nhật số lượng tồn kho.
- **Cart Service** (`http://localhost:8084`):
  - `GET /api/cart/{username}/items`: Lấy giỏ hàng.
  - `POST /api/cart/{username}/items`: Thêm sản phẩm vào giỏ.
  - `PUT /api/cart/{username}/items/{productId}`: Cập nhật số lượng.
  - `DELETE /api/cart/{username}/items/{productId}`: Xóa sản phẩm.
- **Order Service** (`http://localhost:8081`):
  - `POST /api/orders`: Tạo đơn hàng.
  - `GET /api/orders/{id}`: Lấy thông tin đơn hàng.
- **Notification Service** (`http://localhost:8085`):
  - `POST /api/notifications/email`: Gửi email xác nhận.

## Cấu trúc thư mục
```
online-ordering-system/
├── user-service/
│   └── src/main/java/com/example/userservice/
├── product-service/
│   └── src/main/java/com/example/productservice/
├── cart-service/
│   └── src/main/java/com/example/cartservice/
├── order-service/
│   └── src/main/java/com/example/orderservice/
├── notification-service/
│   └── src/main/java/com/example/notificationservice/
├── frontend/
│   └── index.html
├── docker-compose.yml
└── README.md
```

## Góp ý
- Thêm **Swagger** để tài liệu hóa API.
- Tích hợp **JWT** để bảo mật API.
- Cải thiện giao diện frontend với **React** hoặc **Vue.js**.
- Thêm **unit tests** và **integration tests** cho các dịch vụ.
- Tối ưu hóa kiểm tra tồn kho bằng giao dịch nguyên tử để tránh race condition.