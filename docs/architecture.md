# Kiến trúc Hệ thống Đặt hàng Trực tuyến

## Tổng quan
Hệ thống được xây dựng theo kiến trúc microservices sử dụng Spring Boot, MySQL, và giao diện người dùng bằng HTML/JavaScript với Tailwind CSS.

## Các thành phần chính

### Eureka Server
- Chức năng: Service discovery, giúp các service đăng ký và tìm kiếm nhau
- Port: 8761

### API Gateway
- Chức năng: Cổng API trung tâm xử lý và định tuyến các yêu cầu
- Port: 8080

### Các Microservices

#### Order Service
- Chức năng: Xử lý đặt hàng, lưu trữ thông tin đơn hàng
- Port: 8081
- API: `/api/orders`

#### Product Service
- Chức năng: Quản lý thông tin sản phẩm, kiểm tra tồn kho
- Port: 8082
- API: `/api/products`

#### User Service
- Chức năng: Quản lý người dùng, xác thực và phân quyền
- Port: 8083
- API: `/api/users`

#### Cart Service
- Chức năng: Quản lý giỏ hàng người dùng
- Port: 8084
- API: `/api/cart`

#### Notification Service
- Chức năng: Gửi thông báo (email) qua Resend
- Port: 8085
- API: `/api/notifications`

### Database
- MySQL 8.0
- Quản lý các bảng: Users, Products, Orders, OrderItems

### Frontend
- HTML/JavaScript/Tailwind CSS
- Port: 80

## Luồng dữ liệu
1. Người dùng tương tác với Frontend
2. Frontend gửi yêu cầu đến API Gateway
3. API Gateway định tuyến yêu cầu đến các service tương ứng
4. Các service xử lý yêu cầu và gửi phản hồi
5. Phản hồi được chuyển lại cho người dùng

## Bảo mật
- Cơ chế xác thực và phân quyền người dùng
- Cần triển khai JWT trong các phiên bản tương lai 