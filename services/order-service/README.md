# Order Service

Order Service quản lý toàn bộ quá trình đặt hàng, từ tạo đơn hàng đến kiểm tra tính hợp lệ và lưu trữ thông tin.

## Tính năng

- Tạo đơn hàng mới
- Kiểm tra ngày giao hàng (phải sau ít nhất 2 ngày)
- Kiểm tra tồn kho của sản phẩm
- Lưu trữ thông tin đơn hàng và chi tiết đơn hàng
- Gửi thông báo email xác nhận

## Mô hình dữ liệu

### Order
- `id`: Mã đơn hàng
- `customerUsername`: Tên đăng nhập khách hàng
- `totalPrice`: Tổng giá trị đơn hàng
- `deliveryDate`: Ngày giao hàng
- `status`: Trạng thái đơn hàng
- `createdAt`: Thời gian tạo

### OrderItem
- `id`: Mã chi tiết đơn hàng
- `orderId`: Mã đơn hàng
- `productId`: Mã sản phẩm
- `quantity`: Số lượng
- `unitPrice`: Đơn giá

## API Endpoints

- `POST /api/orders`: Tạo đơn hàng mới
- `GET /api/orders/{id}`: Lấy thông tin đơn hàng theo ID

## Tương tác với các Services khác

- Product Service: Kiểm tra tồn kho và cập nhật số lượng
- User Service: Xác thực người dùng và lấy thông tin
- Notification Service: Gửi email xác nhận đơn hàng

## Cổng

Order Service chạy trên cổng 8081 theo mặc định. 