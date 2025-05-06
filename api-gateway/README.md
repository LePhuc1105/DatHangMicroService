# API Gateway

API Gateway là điểm truy cập trung tâm của hệ thống, chịu trách nhiệm định tuyến các yêu cầu đến các microservices phù hợp.

## Chức năng

- Định tuyến yêu cầu dựa trên đường dẫn URI
- Cân bằng tải
- Bảo mật và xác thực
- Logging và monitoring

## Cấu hình

API Gateway được cấu hình để định tuyến các yêu cầu đến các microservices sau:

- `/api/orders/**` -> Order Service
- `/api/products/**` -> Product Service
- `/api/users/**` -> User Service
- `/api/cart/**` -> Cart Service
- `/api/notifications/**` -> Notification Service

## Triển khai

API Gateway được triển khai bằng Spring Cloud Gateway, tích hợp với Eureka Server để service discovery.

## Cổng

API Gateway chạy trên cổng 8080 theo mặc định, có thể thay đổi qua biến môi trường `API_GATEWAY_PORT`. 