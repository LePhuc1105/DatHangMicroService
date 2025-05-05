package com.example.orderservice.service.impl;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OrderItemRepository;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        try {
            // 1. Kiểm tra tính hợp lệ của yêu cầu đặt hàng
            if (order.getItems() == null || order.getItems().isEmpty()) {
                logger.warn("Không có sản phẩm trong đơn hàng");
                throw new IllegalArgumentException("Đơn hàng phải chứa ít nhất một sản phẩm");
            }
            if (order.getDeliveryDate() == null) {
                logger.warn("Thời gian giao hàng không được để trống");
                throw new IllegalArgumentException("Thời gian giao hàng là bắt buộc");
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime minDeliveryDate = now.plusDays(2); // Thời gian giao hàng phải sau 2 ngày
            if (order.getDeliveryDate().isBefore(minDeliveryDate)) {
                logger.warn("Thời gian giao hàng không hợp lệ: {}, phải sau {}",
                        order.getDeliveryDate(), minDeliveryDate);
                throw new IllegalArgumentException("Thời gian giao hàng phải ít nhất 2 ngày sau thời điểm hiện tại");
            }

            // 2. Kiểm tra tài khoản người dùng
            String userPermissionUrl = "http://user-service:8083/api/users/" + order.getCustomerUsername() + "/permission";
            logger.debug("Gửi yêu cầu kiểm tra quyền hạn tới: {}", userPermissionUrl);
            Boolean isUserValid = restTemplate.getForObject(userPermissionUrl, Boolean.class);
            if (isUserValid == null || !isUserValid) {
                logger.warn("Tài khoản không hợp lệ hoặc không có quyền: {}", order.getCustomerUsername());
                throw new IllegalArgumentException("Tài khoản không hợp lệ hoặc không có quyền");
            }

            // 3. Lưu thông tin khách hàng
            String userInfoUrl = "http://user-service:8083/api/users/" + order.getCustomerUsername() + "/info";
            logger.debug("Gửi yêu cầu lưu thông tin khách hàng tới: {}", userInfoUrl);
            try {
                restTemplate.postForEntity(userInfoUrl,
                        Map.of(
                                "name", order.getCustomerName() != null ? order.getCustomerName() : "Unknown",
                                "address", order.getCustomerAddress() != null ? order.getCustomerAddress() : "Unknown",
                                "email", order.getCustomerEmail() != null ? order.getCustomerEmail() : "unknown@example.com",
                                "phone", order.getCustomerPhone() != null ? order.getCustomerPhone() : "Unknown"
                        ),
                        Void.class);
                logger.info("Lưu thông tin khách hàng thành công cho người dùng: {}", order.getCustomerUsername());
            } catch (Exception e) {
                logger.warn("Lỗi khi lưu thông tin khách hàng, tiếp tục quy trình: {}", e.getMessage(), e);
            }

            // 4. Kiểm tra và tính tổng giá
            double totalPrice = 0.0;
            for (OrderItem item : order.getItems()) {
                if (item.getProductId() == null) {
                    logger.warn("Tìm thấy item với productId null");
                    throw new IllegalArgumentException("productId không được null");
                }
                // Lấy thông tin sản phẩm
                String productUrl = "http://product-service:8082/api/products/" + item.getProductId();
                logger.debug("Gửi yêu cầu lấy thông tin sản phẩm tới: {}", productUrl);
                Map<String, Object> productResponse = restTemplate.getForObject(productUrl, Map.class);
                if (productResponse == null) {
                    logger.warn("Không tìm thấy sản phẩm với ID: {}", item.getProductId());
                    throw new IllegalArgumentException("Sản phẩm không tồn tại: " + item.getProductId());
                }
                double productPrice = ((Number) productResponse.get("price")).doubleValue();
                item.setUnitPrice(productPrice);
                totalPrice += productPrice * item.getQuantity();

                // Kiểm tra số lượng sản phẩm
                String productCheckUrl = "http://product-service:8082/api/products/check?productId=" +
                        item.getProductId() + "&quantity=" + item.getQuantity();
                logger.debug("Gửi yêu cầu kiểm tra sản phẩm tới: {}", productCheckUrl);
                Map<String, Boolean> productCheckResponse = restTemplate.getForObject(productCheckUrl, Map.class);
                if (productCheckResponse == null) {
                    logger.error("Không nhận được phản hồi từ ProductService cho productId: {}", item.getProductId());
                    throw new RuntimeException("Lỗi khi kiểm tra tồn kho cho sản phẩm: " + item.getProductId());
                }
                Boolean isProductAvailable = productCheckResponse.get("isAvailable");
                if (isProductAvailable == null || !isProductAvailable) {
                    logger.warn("Sản phẩm không đủ số lượng: {} (yêu cầu: {})", item.getProductId(), item.getQuantity());
                    throw new IllegalStateException("Sản phẩm không đủ số lượng trong kho: " + item.getProductId());
                }
            }
            order.setTotalPrice(totalPrice);

            // 5. Lưu đơn hàng
            order.setStatus("PENDING");
            order.setCreatedAt(LocalDateTime.now());
            logger.debug("Lưu đơn hàng vào cơ sở dữ liệu: {}", order);
            Order savedOrder = orderRepository.save(order);

            // 6. Lưu các mục đơn hàng
            for (OrderItem item : order.getItems()) {
                item.setOrderId(savedOrder.getId());
                orderItemRepository.save(item);
            }

            // 7. Cập nhật số lượng sản phẩm
            for (OrderItem item : order.getItems()) {
                String updateProductUrl = "http://product-service:8082/api/products/" +
                        item.getProductId() + "/updateQuantity?quantity=" + item.getQuantity();
                logger.debug("Gửi yêu cầu cập nhật số lượng sản phẩm tới: {}", updateProductUrl);
                try {
                    restTemplate.put(updateProductUrl, null);
                    logger.info("Cập nhật số lượng sản phẩm thành công cho sản phẩm ID: {}", item.getProductId());
                } catch (Exception e) {
                    logger.error("Lỗi khi cập nhật số lượng sản phẩm: {}", e.getMessage(), e);
                    orderRepository.delete(savedOrder); // Bù trừ
                    throw new RuntimeException("Không thể cập nhật số lượng sản phẩm: " + e.getMessage());
                }
            }

            // 8. Xóa sản phẩm khỏi giỏ hàng
            for (OrderItem item : order.getItems()) {
                String cartUrl = "http://cart-service:8084/api/cart/" + order.getCustomerUsername() +
                        "/items/" + item.getProductId();
                logger.debug("Gửi yêu cầu xóa sản phẩm khỏi giỏ hàng tới: {}", cartUrl);
                try {
                    restTemplate.delete(cartUrl);
                    logger.info("Xóa sản phẩm khỏi giỏ hàng thành công cho người dùng: {}", order.getCustomerUsername());
                } catch (Exception e) {
                    logger.warn("Lỗi khi xóa sản phẩm khỏi giỏ hàng, tiếp tục quy trình: {}", e.getMessage(), e);
                }
            }

            // 9. Gửi email xác nhận
             userInfoUrl = "http://user-service:8083/api/users/" + order.getCustomerUsername();
            Map<String, String> userInfo = restTemplate.getForObject(userInfoUrl, Map.class);
            String email = userInfo != null ? userInfo.get("email") : null;
            if (email != null) {
                String notificationUrl = "http://notification-service:8085/api/notifications/email";
                logger.debug("Gửi yêu cầu gửi email xác nhận tới: {}", notificationUrl);
                String itemsJson = objectMapper.writeValueAsString(order.getItems());
                logger.debug("Payload gửi tới NotificationService: {}", Map.of(
                        "email", email,
                        "orderId", savedOrder.getId(),
                        "status", "COMPLETED",
                        "items", itemsJson,
                        "totalPrice", savedOrder.getTotalPrice()
                ));
                restTemplate.postForEntity(notificationUrl,
                        Map.of(
                                "email", email,
                                "orderId", savedOrder.getId(),
                                "status", "COMPLETED",
                                "items", itemsJson,
                                "totalPrice", savedOrder.getTotalPrice()
                        ),
                        Void.class);
                logger.info("Gửi email xác nhận thành công cho đơn hàng ID: {}", savedOrder.getId());
            } else {
                logger.warn("Không tìm thấy email cho người dùng {}, bỏ qua gửi email", order.getCustomerUsername());
            }

            // 10. Cập nhật trạng thái đơn hàng
            savedOrder.setStatus("COMPLETED");
            orderRepository.save(savedOrder);

            return savedOrder;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Lỗi nghiệp vụ khi tạo đơn hàng: {}", e.getMessage(), e);
            throw e;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Lỗi khi gọi API: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi gọi API: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi tạo đơn hàng: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo đơn hàng: " + e.getMessage());
        }
    }

    @Override
    public Order getOrderById(Long orderId) {
        logger.debug("Tìm kiếm đơn hàng với ID: {}", orderId);
        try {
            return orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi tìm kiếm đơn hàng {}: {}", orderId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi tìm kiếm đơn hàng {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Không thể tìm kiếm đơn hàng: " + e.getMessage());
        }
    }
}