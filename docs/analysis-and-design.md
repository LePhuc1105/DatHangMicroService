# Phân tích và Thiết kế Hệ thống

## Phân tích yêu cầu

### Yêu cầu chức năng
1. **Quản lý người dùng**
   - Đăng nhập, lưu thông tin khách hàng
   - Phân quyền người dùng

2. **Quản lý sản phẩm**
   - Xem danh sách sản phẩm
   - Kiểm tra tồn kho

3. **Giỏ hàng**
   - Thêm sản phẩm vào giỏ hàng
   - Cập nhật số lượng sản phẩm
   - Xóa sản phẩm khỏi giỏ hàng

4. **Đặt hàng**
   - Tạo đơn hàng
   - Kiểm tra tồn kho
   - Xác thực thời gian giao hàng (phải sau 2 ngày)

5. **Thông báo**
   - Gửi email xác nhận đơn hàng

### Yêu cầu phi chức năng
1. **Hiệu suất**: Thời gian phản hồi nhanh, dưới 2 giây
2. **Tính sẵn sàng**: Hệ thống phải sẵn sàng 24/7
3. **Bảo mật**: Bảo vệ thông tin người dùng và giao dịch
4. **Khả năng mở rộng**: Thiết kế cho phép mở rộng dễ dàng

## Thiết kế Cơ sở dữ liệu

### Bảng Users
- `username` (Primary Key): Tên đăng nhập
- `password`: Mật khẩu
- `role`: Vai trò (CUSTOMER, ADMIN)
- `is_active`: Trạng thái hoạt động
- `email`: Email

### Bảng Products
- `id` (Primary Key): Mã sản phẩm
- `name`: Tên sản phẩm
- `description`: Mô tả
- `price`: Giá
- `quantity_in_stock`: Số lượng tồn kho

### Bảng Orders
- `id` (Primary Key): Mã đơn hàng
- `customer_username` (Foreign Key): Tên người dùng
- `total_price`: Tổng giá trị
- `delivery_date`: Ngày giao hàng
- `status`: Trạng thái (PENDING, COMPLETED, CANCELLED)
- `created_at`: Thời gian tạo

### Bảng OrderItems
- `id` (Primary Key): Mã chi tiết đơn hàng
- `order_id` (Foreign Key): Mã đơn hàng
- `product_id` (Foreign Key): Mã sản phẩm
- `quantity`: Số lượng
- `unit_price`: Đơn giá

## Thiết kế API

### User Service API
- GET `/api/users/{username}`: Lấy thông tin người dùng
- GET `/api/users/{username}/permission`: Kiểm tra quyền
- POST `/api/users/{username}/info`: Lưu thông tin người dùng

### Product Service API
- GET `/api/products`: Lấy danh sách sản phẩm
- GET `/api/products/{id}`: Lấy thông tin sản phẩm
- GET `/api/products/check`: Kiểm tra tồn kho
- PUT `/api/products/{id}/updateQuantity`: Cập nhật số lượng

### Cart Service API
- GET `/api/cart/{username}/items`: Lấy giỏ hàng
- POST `/api/cart/{username}/items`: Thêm sản phẩm vào giỏ
- PUT `/api/cart/{username}/items/{productId}`: Cập nhật số lượng
- DELETE `/api/cart/{username}/items/{productId}`: Xóa sản phẩm

### Order Service API
- POST `/api/orders`: Tạo đơn hàng
- GET `/api/orders/{id}`: Lấy thông tin đơn hàng

### Notification Service API
- POST `/api/notifications/email`: Gửi email xác nhận

## Luồng xử lý đặt hàng
1. Người dùng đăng nhập
2. Thêm sản phẩm vào giỏ hàng
3. Tiến hành đặt hàng
4. Hệ thống kiểm tra tồn kho
5. Kiểm tra ngày giao hàng
6. Tạo đơn hàng
7. Gửi email xác nhận
8. Hiển thị xác nhận đặt hàng thành công 