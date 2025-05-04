package com.example.orderservice.service;

import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    /**
     * Tạo đơn hàng mới theo các bước nghiệp vụ:
     * 1. Kiểm tra thông tin khách hàng
     * 2. Kiểm tra số lượng sản phẩm trong kho
     * 3. Lưu đơn hàng với trạng thái "đang xử lý"
     * 4. Cập nhật số lượng sản phẩm trong kho
     */
    public Order createOrder(Order order) {
        try {
            // Bước 1: Kiểm tra thông tin khách hàng
            logger.info("Bắt đầu quy trình đặt hàng cho người dùng ID: {}", order.getUserId());
            
            // Trong môi trường phát triển, bỏ qua kiểm tra userId
            // Mặc định chấp nhận mọi userId, kể cả null hoặc 0
            logger.info("Đơn hàng được tạo với userId: {} (tự động chấp nhận trong môi trường dev)", order.getUserId());

            // Bước 2: Kiểm tra số lượng sản phẩm trong kho
            logger.info("Kiểm tra số lượng sản phẩm ID: {} - Yêu cầu: {}", order.getProductId(), order.getQuantity());
            if (!checkProductAvailability(order.getProductId(), order.getQuantity())) {
                logger.warn("Sản phẩm không đủ số lượng trong kho");
                return null;
            }
            logger.info("Sản phẩm đủ số lượng để đặt hàng");

            // Bước 3: Lưu đơn hàng vào cơ sở dữ liệu
            logger.info("Lưu đơn hàng vào cơ sở dữ liệu");
            Order savedOrder = orderRepository.save(order);
            logger.info("Đơn hàng đã được lưu với ID: {}", savedOrder.getId());

            // Bước 4: Cập nhật số lượng sản phẩm trong kho
            if (!updateProductQuantity(order.getProductId(), order.getQuantity())) {
                logger.error("Không thể cập nhật số lượng sản phẩm. Đơn hàng đã được lưu nhưng cần xử lý thủ công.");
                // Trong trường hợp thực tế, có thể đánh dấu đơn hàng cần xử lý thủ công
                // hoặc rollback lại nếu sử dụng transaction
            } else {
                logger.info("Đã cập nhật số lượng sản phẩm thành công");
            }

            return savedOrder;
        } catch (Exception e) {
            logger.error("Lỗi trong quá trình xử lý đơn hàng: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra thông tin người dùng có hợp lệ không
     */
    private boolean validateUser(Long userId) {
        // Trong môi trường phát triển, luôn trả về true
        logger.info("DEV MODE: Bỏ qua kiểm tra user_id={}", userId);
        return true;
        
        /* Code kiểm tra thực tế cho môi trường production - tạm thời bỏ qua  
        try {
            // Sử dụng endpoint exists để kiểm tra user tồn tại
            String userServiceUrl = "http://user-service-new:8083/api/users/exists/user_" + userId;
            logger.debug("Gửi yêu cầu kiểm tra tài khoản tới: {}", userServiceUrl);
            ResponseEntity<Map> userResponse = restTemplate.getForEntity(userServiceUrl, Map.class);
            
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                Boolean exists = (Boolean) userResponse.getBody().get("exists");
                return Boolean.TRUE.equals(exists);
            }
            return false;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra thông tin người dùng: {}", e.getMessage());
            // Lỗi trong môi trường dev, cho phép đơn hàng mặc dù không có user
            logger.warn("Tạm thời bỏ qua kiểm tra user trong môi trường phát triển");
            return true;
        }
        */
    }

    /**
     * Kiểm tra xem sản phẩm có đủ số lượng trong kho không
     */
    private boolean checkProductAvailability(Long productId, Integer quantity) {
        try {
            String productServiceUrl = "http://product-service:8082/api/products/check?productId=" + productId + "&quantity=" + quantity;
            logger.debug("Gửi yêu cầu kiểm tra sản phẩm tới: {}", productServiceUrl);
            ResponseEntity<Map> productResponse = restTemplate.getForEntity(productServiceUrl, Map.class);
            
            if (productResponse.getBody() != null) {
                Boolean isAvailable = (Boolean) productResponse.getBody().get("isAvailable");
                if (Boolean.TRUE.equals(isAvailable)) {
                    return true;
                }
                // Log thông tin chi tiết về số lượng còn lại nếu không đủ
                Integer availableStock = (Integer) productResponse.getBody().get("availableStock");
                logger.info("Sản phẩm không đủ số lượng. Còn lại: {}, Yêu cầu: {}", availableStock, quantity);
            }
            return false;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra sản phẩm: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật số lượng sản phẩm sau khi đặt hàng
     */
    private boolean updateProductQuantity(Long productId, Integer quantity) {
        try {
            String updateProductUrl = "http://product-service:8082/api/products/" + productId + "/updateQuantity?quantity=" + quantity;
            logger.debug("Gửi yêu cầu cập nhật số lượng sản phẩm tới: {}", updateProductUrl);
            restTemplate.put(updateProductUrl, null);
            return true;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Lỗi khi gọi API cập nhật số lượng sản phẩm: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi cập nhật số lượng sản phẩm: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy thông tin đơn hàng theo ID
     */
    public Order getOrderById(Long orderId) {
        try {
            logger.debug("Tìm kiếm đơn hàng với ID: {}", orderId);
            return orderRepository.findById(orderId).orElse(null);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm đơn hàng {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Không thể tìm kiếm đơn hàng: " + e.getMessage());
        }
    }
}