# Product Service

Product Service quản lý thông tin sản phẩm và kiểm tra tồn kho trong hệ thống đặt hàng trực tuyến.

## Tính năng

- Quản lý danh mục sản phẩm
- Cung cấp thông tin chi tiết sản phẩm
- Kiểm tra tồn kho sản phẩm
- Cập nhật số lượng sản phẩm sau khi đặt hàng

## Mô hình dữ liệu

### Product
- `id`: Mã sản phẩm
- `name`: Tên sản phẩm
- `description`: Mô tả sản phẩm
- `price`: Giá sản phẩm
- `quantity_in_stock`: Số lượng tồn kho

## API Endpoints

- `GET /api/products`: Lấy danh sách tất cả sản phẩm
- `GET /api/products/{id}`: Lấy thông tin chi tiết của một sản phẩm
- `GET /api/products/check`: Kiểm tra tồn kho của sản phẩm
- `PUT /api/products/{id}/updateQuantity`: Cập nhật số lượng tồn kho

## Tương tác với các Service khác

- **Order Service**: Kiểm tra tồn kho khi tạo đơn hàng
- **Cart Service**: Cung cấp thông tin sản phẩm cho giỏ hàng

## Cổng

Product Service chạy trên cổng 8082 theo mặc định, có thể thay đổi qua biến môi trường `PRODUCT_SERVICE_PORT`. 