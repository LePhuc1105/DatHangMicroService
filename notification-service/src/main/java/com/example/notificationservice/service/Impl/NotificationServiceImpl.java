package com.example.notificationservice.service.Impl;

import com.example.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String resendApiKey = "re_6Z1wL99Y_c3b32g7AoTMWit2a7TMZZpLN";
    private final String fromEmail = "no-reply@resend.dev";

    @Override
    public void sendOrderConfirmationEmail(String email, Long orderId, String status, String items, double totalPrice) {
        try {
            logger.debug("Bắt đầu gửi email xác nhận cho đơn hàng ID: {} tới email: {}", orderId, email);

            // Phân tích items từ JSON
            List<Map<String, Object>> itemList = new ArrayList<>();
            try {
                itemList = objectMapper.readValue(items, List.class);
            } catch (Exception e) {
                logger.warn("Không thể phân tích items: {}, sử dụng thông tin mặc định", items, e);
                itemList = new ArrayList<>();
            }

            // Lấy thông tin sản phẩm cho từng item
            StringBuilder itemsDetails = new StringBuilder();
            for (Map<String, Object> item : itemList) {
                Long productId = item.get("productId") != null ? ((Number) item.get("productId")).longValue() : null;
                int quantity = item.get("quantity") != null ? ((Number) item.get("quantity")).intValue() : 0;

                if (productId == null || productId == 0) {
                    logger.warn("productId không hợp lệ trong item: {}", item);
                    itemsDetails.append("Sản phẩm không xác định (ID: null), Số lượng: ").append(quantity).append("\n");
                    continue;
                }

                try {
                    String productUrl = "http://product-service:8082/api/products/" + productId;
                    logger.debug("Gửi yêu cầu lấy thông tin sản phẩm tới: {}", productUrl);
                    Map<String, Object> product = restTemplate.getForObject(productUrl, Map.class);
                    if (product != null) {
                        String productName = (String) product.get("name");
                        double price = ((Number) product.get("price")).doubleValue();
                        itemsDetails.append("Sản phẩm: ").append(productName)
                                .append(", Số lượng: ").append(quantity)
                                .append(", Giá: $").append(price)
                                .append("\n");
                    } else {
                        logger.warn("Không tìm thấy thông tin sản phẩm với ID: {}", productId);
                        itemsDetails.append("Sản phẩm không xác định (ID: ").append(productId)
                                .append("), Số lượng: ").append(quantity).append("\n");
                    }
                } catch (Exception e) {
                    logger.error("Lỗi khi lấy thông tin sản phẩm ID {}: {}", productId, e.getMessage());
                    itemsDetails.append("Sản phẩm không xác định (ID: ").append(productId)
                            .append("), Số lượng: ").append(quantity).append("\n");
                }
            }

            // Tạo nội dung email
            String subject = "Xác nhận đơn hàng #" + orderId;
            String text = "Cảm ơn bạn đã đặt hàng!\n" +
                    "Mã đơn hàng: " + orderId + "\n" +
                    "Trạng thái: " + status + "\n" +
                    "Các sản phẩm:\n" + itemsDetails.toString() +
                    "Tổng giá: $" + totalPrice + "\n" +
                    "Chúng tôi sẽ thông báo khi đơn hàng được xử lý.";

            // Gửi email qua Resend API
            String resendUrl = "https://api.resend.com/emails";
            Map<String, Object> emailPayload = Map.of(
                    "from", fromEmail,
                    "to", email,
                    "subject", subject,
                    "text", text
            );

            logger.debug("Gửi yêu cầu tới Resend API: {}", resendUrl);
            restTemplate.postForEntity(resendUrl,
                    new HttpEntity<>(emailPayload, new HttpHeaders() {{
                        set("Authorization", "Bearer " + resendApiKey);
                        setContentType(MediaType.APPLICATION_JSON);
                    }}),
                    String.class);

            logger.info("Gửi email xác nhận thành công cho đơn hàng ID: {}", orderId);
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi gửi email xác nhận cho đơn hàng {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email xác nhận: " + e.getMessage());
        }
    }
}