# Eureka Server

Eureka Server là dịch vụ Service Discovery trong hệ thống microservices, giúp các service đăng ký và tìm kiếm lẫn nhau.

## Chức năng chính

- Đăng ký service: Các microservice đăng ký thông tin với Eureka Server khi khởi động
- Service discovery: Giúp các service tìm kiếm và giao tiếp với nhau mà không cần biết chính xác địa chỉ
- Health monitoring: Giám sát trạng thái hoạt động của các service
- Load balancing: Hỗ trợ cân bằng tải giữa các instance của cùng một service

## Cấu hình

Eureka Server được cấu hình mặc định với các tham số sau:
- Tự bảo vệ (self-preservation): Kích hoạt
- Renewal percent threshold: 0.85
- Renewal interval: 30 giây

## API

Eureka Server cung cấp các REST API để quản lý service registry:
- Danh sách tất cả các service: `GET /eureka/apps`
- Thông tin về một service cụ thể: `GET /eureka/apps/{serviceId}`
- Dashboard: `GET /`

## Cổng

Eureka Server chạy trên cổng 8761 theo mặc định, có thể thay đổi qua biến môi trường `EUREKA_PORT`. 