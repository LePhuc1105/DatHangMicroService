# User Service

User Service quản lý thông tin người dùng, xác thực và phân quyền trong hệ thống đặt hàng trực tuyến.

## Tính năng

- Quản lý tài khoản người dùng
- Xác thực và phân quyền
- Lưu trữ thông tin cá nhân của khách hàng
- Kiểm tra quyền truy cập vào các tính năng hệ thống

## Mô hình dữ liệu

### User
- `username`: Tên đăng nhập (Primary Key)
- `password`: Mật khẩu
- `role`: Vai trò (CUSTOMER, ADMIN)
- `is_active`: Trạng thái hoạt động
- `email`: Địa chỉ email

### UserInfo
- `username`: Tên đăng nhập (Foreign Key)
- `name`: Họ tên
- `address`: Địa chỉ
- `phone`: Số điện thoại

## API Endpoints

- `GET /api/users/{username}`: Lấy thông tin người dùng
- `GET /api/users/{username}/permission`: Kiểm tra quyền của người dùng
- `POST /api/users/{username}/info`: Lưu thông tin cá nhân của người dùng

## Tương tác với các Service khác

- **Order Service**: Xác thực người dùng khi tạo đơn hàng
- **Cart Service**: Cung cấp thông tin người dùng cho giỏ hàng
- **Notification Service**: Cung cấp thông tin liên hệ của người dùng để gửi thông báo

## Cổng

User Service chạy trên cổng 8083 theo mặc định, có thể thay đổi qua biến môi trường `USER_SERVICE_PORT`. 