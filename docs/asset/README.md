# Hướng dẫn tạo sơ đồ kiến trúc

Thư mục này chứa các tệp tin liên quan đến sơ đồ kiến trúc của hệ thống đặt hàng trực tuyến.

## Sơ đồ ASCII

File `architecture.txt` chứa sơ đồ kiến trúc dạng ASCII, có thể xem trực tiếp trong trình soạn thảo text.

## Sơ đồ PlantUML

File `architecture.puml` chứa mã PlantUML để tạo sơ đồ kiến trúc dạng hình ảnh. Để chuyển đổi file này thành hình ảnh, bạn có thể:

1. **Sử dụng công cụ online**:
   - Truy cập [PlantUML Online Editor](https://www.plantuml.com/plantuml/uml/)
   - Sao chép nội dung từ file `architecture.puml` và dán vào editor
   - Hình ảnh sẽ được tạo tự động

2. **Sử dụng plugin PlantUML trong IDE**:
   - Cài đặt plugin PlantUML cho Visual Studio Code, IntelliJ IDEA, hoặc IDE của bạn
   - Mở file `architecture.puml` và sử dụng tính năng preview

3. **Sử dụng dòng lệnh**:
   - Cài đặt PlantUML JAR
   - Chạy lệnh: `java -jar plantuml.jar architecture.puml`

Sau khi tạo xong, lưu file hình ảnh với tên `Architechture.png` để sử dụng trong tài liệu. 