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
- **Frontend**: Giao diện người dùng.

Cơ sở dữ liệu: **MySQL** với các bảng `Users`, `Products`, `Orders`, `OrderItems`.

## Yêu cầu
- **Docker** và **Docker Compose** (để chạy microservices).
- **Java 17** (cho Spring Boot).
- **MySQL 8.0** (cơ sở dữ liệu).
- **Resend API Key** (cho gửi email).

## Cài đặt
1. **Clone repository**:
   ```bash
   git clone <repository-url>
   cd online-ordering-system
   ```

2. **Cấu hình môi trường**:
   ```bash
   cp .env.example .env
   # Chỉnh sửa các biến môi trường trong file .env
   ```

## Chạy ứng dụng
1. **Khởi động tất cả services**:
   ```bash
   docker-compose up -d
   ```

2. **Truy cập ứng dụng**:
   - Frontend: `http://localhost:80`
   - API Gateway: `http://localhost:8080`
   - Eureka Server: `http://localhost:8761`

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
   - Đặt hàng với ngày giao hàng sau 2 ngày.
   - Kiểm tra email xác nhận.

## API Endpoints
Xem tài liệu API trong thư mục `docs/api-specs/`.

## Cấu trúc thư mục
```
./
├── README.md                       # Tài liệu hướng dẫn
├── .env.example                    # Mẫu biến môi trường
├── docker-compose.yml              # Cấu hình Docker Compose
├── docs/                           # Tài liệu
│   ├── architecture.md             # Mô tả kiến trúc hệ thống
│   ├── analysis-and-design.md      # Phân tích và thiết kế chi tiết
│   ├── asset/                      # Hình ảnh, sơ đồ
│   └── api-specs/                  # Đặc tả API
│       ├── order-service.yaml
│       └── ...
├── scripts/                        # Scripts tiện ích
├── services/                       # Các microservices
│   ├── order-service/
│   ├── product-service/
│   ├── user-service/
│   ├── cart-service/
│   ├── notification-service/
│   └── eureka-server/
├── api-gateway/                    # API Gateway
└── frontend/                       # Giao diện người dùng
``` 