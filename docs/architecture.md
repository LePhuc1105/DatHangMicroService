# Thiết Kế Hệ Thống: DatHangMicroService

## 1. Tổng Quan
Hệ thống **DatHangMicroService** là một nền tảng thương mại điện tử hỗ trợ đặt hàng trực tuyến, được xây dựng dựa trên kiến trúc microservices cho backend và giao diện web frontend. Hệ thống cung cấp các tính năng như duyệt sản phẩm, quản lý giỏ hàng, đặt hàng, xác thực người dùng, và gửi thông báo qua email.

### Mục Tiêu
- Cung cấp backend linh hoạt, dễ mở rộng với các microservices quản lý sản phẩm, giỏ hàng, đơn hàng, người dùng, và thông báo.
- Tạo giao diện frontend thân thiện, cho phép người dùng duyệt sản phẩm, quản lý giỏ hàng, đặt hàng, và đăng nhập.
- Sử dụng giao tiếp bất đồng bộ qua Kafka để xử lý các sự kiện như thông báo đơn hàng.
- Triển khai hệ thống trong môi trường container hóa để đảm bảo tính nhất quán giữa phát triển và sản xuất.

## 2. Kiến Trúc Hệ Thống

### 2.1 Kiến Trúc Tổng Quan
Hệ thống bao gồm **backend** (các microservices) và **frontend** (giao diện web). Backend sử dụng kiến trúc microservices, với các dịch vụ giao tiếp qua REST API và Apache Kafka cho các tác vụ bất đồng bộ. Frontend là một ứng dụng web đơn trang (SPA) được xây dựng bằng HTML, Tailwind CSS, và JavaScript, tương tác với backend qua API Gateway.

```
[Client] <-> [Frontend (HTML/JS)] <-> [API Gateway] <-> [Microservices: Product, Order, Cart, User, Notification]
                                             |
                                         [Kafka] <-> [Notification Service]
                                             |
                                         [MySQL] <- [Lưu trữ dữ liệu]
                                         [Eureka] <- [Service Discovery]
```

### 2.2 Các Thành Phần

#### Backend
1. **API Gateway (`api-gateway`)**
   - **Mục đích**: Định tuyến yêu cầu từ client đến các microservices.
   - **Công nghệ**: Spring Cloud Gateway, tích hợp với Eureka.
   - **Chức năng**: Cân bằng tải, định tuyến (ví dụ: `/products/**` đến `product-service`, `/orders/**` đến `order-service`).
   - **Cổng**: 8080.
   - **Tệp chính**:
     - `src/main/java/com/example/apigateway/ApiGatewayApplication.java`
     - `src/main/resources/application.properties`
   - **Phụ thuộc**: Eureka Server.

2. **Service Discovery (`eureka-server`)**
   - **Mục đích**: Quản lý đăng ký và khám phá dịch vụ.
   - **Công nghệ**: Netflix Eureka Server.
   - **Chức năng**: Cho phép các microservices đăng ký và tìm kiếm nhau động.
   - **Cổng**: 8761.
   - **Tệp chính**:
     - `src/main/java/com/example/eurekaserver/EurekaserverApplication.java`
     - `src/main/resources/application.properties`

3. **Order Service (`order-service`)**
   - **Mục đích**: Quản lý tạo, cập nhật, và truy xuất đơn hàng.
   - **Công nghệ**: Spring Boot, Spring Data JPA, Kafka.
   - **Chức năng**:
     - Cung cấp REST API (ví dụ: `POST /orders`, `GET /orders/{id}`).
     - Tương tác với `product-service` để xác thực sản phẩm.
     - Gửi sự kiện `order-created` đến Kafka.
   - **Cơ sở dữ liệu**: MySQL (bảng `orders` trong `order_db`).
   - **Cổng**: 8081.
   - **Tệp chính**:
     - `src/main/java/com/example/orderservice/OrderServiceApplication.java`
     - `src/main/java/com/example/orderservice/controller/OrderController.java`
     - `src/main/java/com/example/orderservice/model/Order.java`
     - `src/main/resources/application.properties`
   - **Phụ thuộc**: Eureka Server, MySQL, Kafka.

4. **Cart Service (`cart-service`)**
   - **Mục đích**: Quản lý giỏ hàng của người dùng.
   - **Công nghệ**: Spring Boot, Spring Data JPA.
   - **Chức năng**:
     - Cung cấp REST API (ví dụ: `POST /cart/{username}/items`, `GET /cart/{username}/items`).
     - Lưu trữ các mục trong giỏ hàng theo người dùng.
   - **Cơ sở dữ liệu**: MySQL (bảng `cart_items` trong `order_db`).
   - **Cổng**: 8084.
   - **Tệp chính**:
     - `src/main/java/com/example/cartservice/CartServiceApplication.java`
     - `src/main/java/com/example/cartservice/controller/CartController.java`
     - `src/main/java/com/example/cartservice/entity/Cart.java`
     - `src/main/resources/application.properties`
   - **Phụ thuộc**: Eureka Server, MySQL.

5. **User Service (`user-service`)**
   - **Mục đích**: Quản lý xác thực và thông tin người dùng.
   - **Công nghệ**: Spring Boot, Spring Data JPA.
   - **Chức năng**:
     - Cung cấp REST API (ví dụ: `GET /users/{username}/permission` để xác thực).
     - Xác thực người dùng qua Basic Auth.
   - **Cơ sở dữ liệu**: MySQL (bảng `users` trong `order_db`).
   - **Cổng**: 8083.
   - **Tệp chính**:
     - `src/main/java/com/example/userservice/UserServiceApplication.java`
     - `src/main/java/com/example/userservice/controller/UserController.java`
     - `src/main/java/com/example/userservice/entity/User.java`
     - `src/main/resources/application.properties`
   - **Phụ thuộc**: Eureka Server, MySQL.

6. **Product Service (`product-service`)**
   - **Mục đích**: Quản lý danh mục sản phẩm.
   - **Công nghệ**: Spring Boot, Spring Data JPA.
   - **Chức năng**:
     - Cung cấp REST API (ví dụ: `GET /products`, `GET /products/{id}`).
     - Cung cấp thông tin sản phẩm (tên, giá, số lượng tồn) cho `order-service` và `cart-service`.
   - **Cơ sở dữ liệu**: MySQL (bảng `products` trong `order_db`).
   - **Cổng**: 8082.
   - **Tệp chính**:
     - `src/main/java/com/example/productservice/ProductServiceApplication.java`
     - `src/main/java/com/example/productservice/controller/ProductController.java`
     - `src/main/java/com/example/productservice/entity/Product.java`
     - `src/main/resources/application.properties`
   - **Phụ thuộc**: Eureka Server, MySQL.

7. **Notification Service (`notification-service`)**
   - **Mục đích**: Gửi thông báo email khi có đơn hàng mới.
   - **Công nghệ**: Spring Boot, Kafka, Resend API.
   - **Chức năng**:
     - Tiêu thụ sự kiện Kafka (ví dụ: topic `order-created`).
     - Gửi email xác nhận qua Resend API (`no-reply@resend.dev`).
   - **Cổng**: 8085.
   - **Tệp chính**:
     - `src/main/java/com/example/notificationservice/NotificationServiceApplication.java`
     - `src/main/java/com/example/notificationservice/controller/NotificationController.java`
     - `src/main/java/com/example/notificationservice/service/NotificationService.java`
     - `src/main/resources/application.properties`
   - **Phụ thuộc**: Eureka Server, Kafka.
   - **Biến môi trường**:
     - `RESEND_API_KEY`: Khóa API Resend.
     - `RESEND_FROM_EMAIL`: Email gửi (`no-reply@resend.dev`).

8. **Message Queue (Kafka)**
   - **Mục đích**: Xử lý sự kiện bất đồng bộ.
   - **Công nghệ**: Apache Kafka, Zookeeper.
   - **Chức năng**: Gửi và nhận sự kiện (ví dụ: `order-created` cho thông báo).
   - **Cổng**: 9092 (Kafka), 2181 (Zookeeper).

9. **Cơ sở dữ liệu (MySQL)**
   - **Mục đích**: Lưu trữ dữ liệu cho đơn hàng, giỏ hàng, người dùng, và sản phẩm.
   - **Cơ sở dữ liệu**: `order_db`.
   - **Bảng**:
     - `products`: Thông tin sản phẩm (ID, tên, giá, số lượng, mô tả).
     - `orders`: Thông tin đơn hàng (ID, user_id, product_id, số lượng, trạng thái, tên khách, địa chỉ, email, điện thoại, ngày giao).
     - `cart_items`: Mục trong giỏ hàng (ID, user_id, product_id, số lượng).
     - `users`: Thông tin người dùng (ID, username, password).
   - **Cổng**: 3306 (ánh xạ sang 3307 trên host).
   - **Cấu hình**:
     - Mật khẩu root: `123456`.
     - Script khởi tạo: `db-init/init.sql`.

#### Frontend
1. **Giao diện Web (`frontend`)**
   - **Mục đích**: Cung cấp giao diện người dùng để duyệt sản phẩm, quản lý giỏ hàng, đặt hàng, và đăng nhập.
   - **Công nghệ**: HTML, Tailwind CSS, JavaScript.
   - **Tính năng**:
     - Hiển thị danh sách sản phẩm (tên, giá, số lượng, nút thêm vào giỏ).
     - Quản lý giỏ hàng (xem, cập nhật số lượng, xóa mục).
     - Form thanh toán (tên, địa chỉ, email, điện thoại, ngày giao).
     - Modal đăng nhập (username, password).
   - **Tệp chính**:
     - `frontend/index.html`: Giao diện chính.
     - `frontend/script.js`: Xử lý logic (API calls, DOM).
     - `frontend/style.css`: CSS tùy chỉnh với Tailwind.
   - **Tương tác API**: Gửi yêu cầu REST qua API Gateway.
   - **Cổng**: 80.
   - **Phụ thuộc**: API Gateway.

2. **Cấu hình API**:
     ```javascript
     const API_CONFIG = {
         baseUrl: 'http://api-gateway:8080/api',
         getUrl: (endpoint) => `${API_CONFIG.baseUrl}/${endpoint}`,
         fetchOptions: {
             headers: { 'Content-Type': 'application/json' },
         },
     };
     ```

### 2.3 Triển Khai
- **Container hóa**: Docker và Docker Compose.
- **Dịch vụ trong `docker-compose.yml`**:
  - `eureka-server`, `api-gateway`, `frontend`, `mysql`, `order-service`, `user-service`, `product-service`, `cart-service`, `notification-service`.
- **Mạng**: Các dịch vụ giao tiếp qua mạng bridge (`app-network`).
- **Kiểm tra sức khỏe**:
  - Eureka: Kiểm tra `/actuator/health`.
  - MySQL: Dùng `mysqladmin ping`.
- **Volume**:
  - `mysql-data`: Lưu trữ dữ liệu MySQL.
  - `db-init`: Khởi tạo schema.
- **Thực thi**: `docker-compose up` khởi động tất cả dịch vụ.

## 3. Luồng Hoạt Động

### 3.1 Luồng Đặt Hàng
1. **Duyệt Sản Phẩm**:
   - Người dùng truy cập frontend, gửi `GET /products` qua API Gateway đến `product-service`.
   - Frontend hiển thị danh sách sản phẩm (tên, giá, số lượng tồn).

2. **Thêm vào Giỏ Hàng**:
   - Người dùng nhấn "Thêm vào Giỏ", gửi `POST /cart/{username}/items` đến `cart-service`.
   - Yêu cầu đăng nhập; nếu chưa đăng nhập, hiển thị modal đăng nhập.
   - Cập nhật số lượng giỏ hàng trên giao diện.

3. **Xem Giỏ Hàng**:
   - Người dùng nhấn "Giỏ Hàng", gửi `GET /cart/{username}/items` đến `cart-service`.
   - Frontend lấy chi tiết sản phẩm (`GET /products/{id}`) từ `product-service` cho từng mục.
   - Hiển thị giỏ hàng với checkbox, ô số lượng, và nút xóa.

4. **Thanh Toán**:
   - Người dùng chọn mục và nhấn "Thanh Toán", hiển thị form thanh toán.
   - Người dùng điền thông tin (tên, địa chỉ, email, điện thoại, ngày giao).
   - Frontend gửi `POST /orders` đến `order-service` với dữ liệu đơn hàng (username, các mục, thông tin khách).
   - `order-service`:
     - Xác thực sản phẩm qua `product-service` (`GET /products/{id}`).
     - Lưu đơn hàng vào bảng `orders`.
     - Gửi sự kiện `order-created` đến Kafka.
   - Frontend xóa giỏ hàng và hiển thị thông báo thành công.

5. **Thông Báo**:
   - `notification-service` tiêu thụ sự kiện `order-created` từ Kafka.
   - Gửi email xác nhận qua Resend API.

### 3.2 Luồng Xác Thực
1. **Đăng Nhập**:
   - Người dùng nhấn "Đăng Nhập", hiển thị modal.
   - Gửi username và password qua `GET /users/{username}/permission` đến `user-service` với `Basic Auth` (base64 của `username:password`).
   - Nếu thành công, lưu `currentUser` trong frontend và cập nhật giao diện.

## 4. Công Nghệ Sử Dụng

### Backend
- **Ngôn ngữ**: Java.
- **Framework**: Spring Boot, Spring Cloud.
- **Cơ sở dữ liệu**: MySQL 8.
- **Hàng đợi tin nhắn**: Apache Kafka, Zookeeper.
- **Khám phá dịch vụ**: Netflix Eureka.
- **API Gateway**: Spring Cloud Gateway.
- **Dịch vụ email**: Resend API.
- **Thư viện**: Lombok, MapStruct, Spring Data JPA.
- **Triển khai**: Docker, Docker Compose.

### Frontend
- **Giao diện**: HTML.
- **CSS**: Tailwind CSS, CSS tùy chỉnh.
- **JavaScript**: Vanilla JS (sử dụng `fetch` cho API).
- **Cấu hình**: `config.js` (giả định).

## 5. Mô Hình Dữ Liệu

### Bảng MySQL (trong `order_db`)
1. **Products**:
   - `id`: Khóa chính (tự tăng).
   - `name`: Tên sản phẩm.
   - `price`: Giá sản phẩm.
   - `quantity`: Số lượng tồn.
   - `description`: Mô tả (tùy chọn).

2. **Orders**:
   - `id`: Khóa chính (tự tăng).
   - `user_id`: Khóa ngoại (tham chiếu `users`).
   - `product_id`: Khóa ngoại (tham chiếu `products`).
   - `quantity`: Số lượng.
   - `status`: Trạng thái (CREATED, PROCESSING, v.v.).
   - `customer_name`: Tên khách hàng.
   - `address`: Địa chỉ giao hàng.
   - `email`: Email khách hàng.
   - `phone`: Số điện thoại.
   - `delivery_date`: Ngày giao.

3. **Cart Items**:
   - `id`: Khóa chính (tự tăng).
   - `user_id`: Khóa ngoại (tham chiếu `users`).
   - `product_id`: Khóa ngoại (tham chiếu `products`).
   - `quantity`: Số lượng.

4. **Users**:
   - `id`: Khóa chính (tự tăng).
   - `username`: Tên người dùng (duy nhất).
   - `password`: Mật khẩu (đã mã hóa).

## 6. Điểm Mạnh

### Backend
- **Kiến trúc Microservices**: Bao gồm đầy đủ các dịch vụ (`order`, `cart`, `user`, `product`, `notification`), dễ mở rộng và bảo trì.
- **Khám phá dịch vụ**: Eureka hỗ trợ đăng ký và cân bằng tải động.
- **Xử lý bất đồng bộ**: Kafka đảm bảo thông báo đơn hàng đáng tin cậy.
- **Container hóa**: Docker Compose đơn giản hóa triển khai.
- **Tích hợp email**: Resend API cung cấp thông báo email hiệu quả.

### Frontend
- **Giao diện đáp ứng**: Tailwind CSS đảm bảo tương thích trên thiết bị di động.
- **Tính năng đầy đủ**: Hỗ trợ duyệt sản phẩm, quản lý giỏ hàng, thanh toán, và đăng nhập.
- **Tích hợp API**: Tương tác mượt mà với tất cả dịch vụ backend.
- **Đơn giản**: Vanilla JavaScript giảm phụ thuộc vào thư viện.

## 7. Hạn Chế

### Backend
1. **Bảo mật**:
   - Sử dụng `Basic Auth` cho xác thực, dễ bị tấn công.
   - Không có cấu hình HTTPS cho API Gateway.
2. **Xử lý lỗi**:
   - Thiếu cơ chế thử lại hoặc ngắt mạch (ví dụ: Resilience4j).
3. **Giám sát**:
   - Không tích hợp công cụ giám sát như Prometheus hoặc Grafana.
4. **Kiểm thử**:
   - Chỉ có các file kiểm thử cơ bản (`*Tests.java`), thiếu kiểm thử đơn vị và tích hợp toàn diện.


### Frontend
1. **Quản lý trạng thái**:
   - Sử dụng biến toàn cục (`currentUser`, `cartCount`), mất dữ liệu khi tải lại trang.
2. **Bảo mật**:
   - `Basic Auth` không an toàn.
   - Thiếu bảo vệ CSRF cho các yêu cầu POST.
3. **Hiệu suất**:
   - Gửi nhiều yêu cầu `GET /products/{id}` cho các mục trong giỏ hàng, gây độ trễ.
4. **Xác thực dữ liệu**:
   - Chỉ kiểm tra ngày giao hàng, thiếu xác thực email, điện thoại.
5. **Xử lý lỗi**:
   - Sử dụng `alert` cho lỗi, thiếu phản hồi giao diện chi tiết.


## 8. Khả Năng Mở Rộng
- **Backend**:
  - Mở rộng ngang: Triển khai nhiều instance cho mỗi dịch vụ, quản lý bởi Eureka.
  - Phân vùng cơ sở dữ liệu: Phân vùng bảng `orders` và `cart_items` cho lưu lượng cao.
  - Phân vùng Kafka: Tăng số phân vùng cho topic `order-created`.
- **Frontend**:
  - Phục vụ qua CDN cho tài nguyên tĩnh.
  - Tối ưu yêu cầu API bằng lưu trữ và gộp.
- **Cân bằng tải**:
  - API Gateway xử lý cân bằng tải phía client.
  - Sử dụng Nginx để cân bằng tải bổ sung trong sản xuất.

## 9. Kết Luận
Hệ thống **DatHangMicroService** là một nền tảng thương mại điện tử toàn diện với backend microservices và frontend thân thiện. Backend bao gồm tất cả dịch vụ cần thiết (`order`, `cart`, `user`, `product`, `notification`) và hỗ trợ quy trình đặt hàng từ đầu đến cuối với thông báo email. Frontend cung cấp trải nghiệm người dùng mượt mà. Tuy nhiên, cần cải thiện bảo mật, xử lý lỗi, và tài liệu để sẵn sàng cho sản xuất. Với các đề xuất trên, hệ thống có thể trở thành một nền tảng an toàn, dễ mở rộng, và dễ bảo trì.