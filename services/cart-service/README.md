# Cart Service

Cart Service quản lý giỏ hàng của người dùng, cho phép thêm, xóa và cập nhật sản phẩm trong giỏ hàng.

## Tính năng

- Tạo và quản lý giỏ hàng cho mỗi người dùng
- Thêm sản phẩm vào giỏ hàng
- Cập nhật số lượng sản phẩm trong giỏ hàng
- Xóa sản phẩm khỏi giỏ hàng
- Lấy thông tin giỏ hàng hiện tại

## Mô hình dữ liệu

### CartItem
- `id`: Mã mục giỏ hàng
- `username`: Tên đăng nhập người dùng
- `product_id`: Mã sản phẩm
- `quantity`: Số lượng
- `price`: Giá sản phẩm tại thời điểm thêm vào giỏ

## API Endpoints

- `GET /api/cart/{username}/items`: Lấy tất cả các sản phẩm trong giỏ hàng của người dùng
- `POST /api/cart/{username}/items`: Thêm sản phẩm vào giỏ hàng
- `PUT /api/cart/{username}/items/{productId}`: Cập nhật số lượng sản phẩm trong giỏ hàng
- `DELETE /api/cart/{username}/items/{productId}`: Xóa sản phẩm khỏi giỏ hàng

## Tương tác với các Service khác

- **Product Service**: Lấy thông tin sản phẩm khi thêm vào giỏ hàng
- **User Service**: Xác thực người dùng
- **Order Service**: Cung cấp thông tin giỏ hàng để tạo đơn hàng

## Cổng

Cart Service chạy trên cổng 8084 theo mặc định, có thể thay đổi qua biến môi trường `CART_SERVICE_PORT`. 