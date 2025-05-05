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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    /**
     * Tạo đơn hàng mới
     */
    public Order createOrder(Order order) {
        try {
            // Bước 1: Kiểm tra quyền người dùng thông qua user service
            logger.info("Bắt đầu quy trình đặt hàng cho người dùng ID: {}", order.getUserId());
            
            // Lấy username từ userId
            String username = getUsernameFromId(order.getUserId());
            if (username == null) {
                logger.warn("Không thể lấy username cho userId: {}", order.getUserId());
                return null;
            }
            
            // Kiểm tra quyền người dùng
            if (!checkPermission(username)) {
                logger.warn("Không có quyền tạo đơn hàng cho username: {}", username);
                return null;
            }
            
            // Bước 2: Kiểm tra số lượng sản phẩm trong kho
            logger.info("Kiểm tra số lượng sản phẩm ID: {} - Yêu cầu: {}", 
                       order.getProductId(), order.getQuantity());
                       
            if (!checkProductAvailability(order.getProductId(), order.getQuantity())) {
                logger.warn("Sản phẩm không đủ số lượng trong kho");
                return null;
            }
            
            logger.info("Sản phẩm đủ số lượng để đặt hàng");

            // Bước 3: Lưu đơn hàng vào cơ sở dữ liệu
            logger.info("Lưu đơn hàng vào cơ sở dữ liệu");
            
            // Đảm bảo các trường cần thiết được thiết lập
            if (order.getCreatedAt() == null) {
                order.setCreatedAt(LocalDateTime.now());
            }
            
            if (order.getUpdatedAt() == null) {
                order.setUpdatedAt(LocalDateTime.now());
            }
            
            if (order.getStatus() == null || order.getStatus().isEmpty()) {
                order.setStatus("PENDING");
            }
            
            Order savedOrder = orderRepository.save(order);
            logger.info("Đơn hàng đã được lưu với ID: {}", savedOrder.getId());

            // Bước 4: Cập nhật số lượng sản phẩm trong kho
            if (!updateProductQuantity(order.getProductId(), order.getQuantity())) {
                logger.error("Không thể cập nhật số lượng sản phẩm. Đơn hàng đã được lưu nhưng cần xử lý thủ công.");
                // Trong trường hợp thực tế, có thể đánh dấu đơn hàng cần xử lý thủ công
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
     * Lấy username từ userId
     */
    private String getUsernameFromId(Long userId) {
        if (userId == null || userId <= 0) {
            logger.warn("userId không hợp lệ: {}", userId);
            return null;
        }
        
        try {
            String userServiceUrl = "http://user-service/api/users/id/" + userId;
            logger.info("Gửi yêu cầu lấy thông tin người dùng: {}", userServiceUrl);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(userServiceUrl, Map.class);
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                logger.warn("Không thể lấy thông tin người dùng userId: {}", userId);
                return null;
            }
            
            String username = (String) response.getBody().get("username");
            logger.info("Đã tìm thấy username {} cho userId {}", username, userId);
            return username;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thông tin người dùng: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Kiểm tra quyền tạo đơn hàng thông qua username
     */
    private boolean checkPermission(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("username không hợp lệ");
            return false;
        }
        
        try {
            // Sử dụng API /{username}/check có sẵn trong user-service
            String userServiceUrl = "http://user-service/api/users/" + username + "/check";
            logger.info("Gửi yêu cầu kiểm tra quyền người dùng: {}", userServiceUrl);
            
            ResponseEntity<Boolean> response = restTemplate.getForEntity(userServiceUrl, Boolean.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.warn("Kiểm tra quyền thất bại với status code: {}", response.getStatusCode());
                return false;
            }
            
            Boolean hasPermission = response.getBody();
            if (Boolean.TRUE.equals(hasPermission)) {
                logger.info("Người dùng {} có quyền tạo đơn hàng", username);
                return true;
            } else {
                logger.warn("Người dùng {} không có quyền tạo đơn hàng", username);
                return false;
            }
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra quyền người dùng: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kiểm tra xem sản phẩm có đủ số lượng trong kho không
     * Kiểm tra thông tin người dùng có hợp lệ không
     */
    private boolean validateUser(Long userId) {
        if (userId == null || userId <= 0) {
            logger.warn("userId không hợp lệ: {}", userId);
            return false;
        }
        
        try {
            // Sửa lại URL cho phù hợp với API thực tế
            String userServiceUrl = "http://user-service/api/users/" + userId;
            logger.info("Gửi yêu cầu kiểm tra người dùng tới: {}", userServiceUrl);
            
            ResponseEntity<Object> response = restTemplate.getForEntity(userServiceUrl, Object.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Người dùng ID {} tồn tại và hợp lệ", userId);
                return true;
            } else {
                logger.warn("Người dùng ID {} không tồn tại hoặc không hợp lệ", userId);
                return false;
            }
        } catch (HttpClientErrorException.NotFound e) {
            // Trường hợp 404: Người dùng không tồn tại
            logger.warn("Người dùng ID {} không tồn tại", userId);
            return false;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra thông tin người dùng: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kiểm tra xem sản phẩm có đủ số lượng trong kho không
     */
    private boolean checkProductAvailability(Long productId, Integer quantity) {
        if (productId == null || productId <= 0) {
            logger.warn("productId không hợp lệ: {}", productId);
            return false;
        }
        
        if (quantity == null || quantity <= 0) {
            logger.warn("quantity không hợp lệ: {}", quantity);
            return false;
        }
        
        try {
            // Sửa lại URL cho phù hợp với API thực tế của product-service
            String productServiceUrl = "http://product-service/api/products/" + productId;
            logger.info("Gửi yêu cầu kiểm tra sản phẩm tới: {}", productServiceUrl);
            
            ResponseEntity<Object> response = restTemplate.getForEntity(productServiceUrl, Object.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.warn("Sản phẩm ID {} không tồn tại", productId);
                return false;
            }
            
            // Kiểm tra số lượng sản phẩm (giả sử response trả về dạng Map)
            if (response.getBody() instanceof Map) {
                Map<String, Object> productData = (Map<String, Object>) response.getBody();
                Integer availableQuantity = (Integer) productData.get("quantity");
                
                if (availableQuantity != null && availableQuantity >= quantity) {
                    logger.info("Sản phẩm ID {} có đủ số lượng {} trong kho", productId, quantity);
                    return true;
                } else {
                    logger.warn("Sản phẩm ID {} không đủ số lượng. Yêu cầu: {}, Hiện có: {}", 
                             productId, quantity, availableQuantity);
                    return false;
                }
            }
            
            logger.warn("Không thể kiểm tra số lượng sản phẩm ID {}: định dạng dữ liệu không hợp lệ", productId);
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Sản phẩm ID {} không tồn tại", productId);
            return false;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra sản phẩm: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Cập nhật số lượng sản phẩm sau khi đặt hàng
     */
    private boolean updateProductQuantity(Long productId, Integer quantity) {
        if (productId == null || productId <= 0) {
            logger.warn("productId không hợp lệ: {}", productId);
            return false;
        }
        
        if (quantity == null || quantity <= 0) {
            logger.warn("quantity không hợp lệ: {}", quantity);
            return false;
        }
        
        try {
            // Sửa lại URL cho đúng với service discovery
            String updateProductUrl = "http://product-service/api/products/" + productId 
                                     + "/updateQuantity?quantity=" + quantity;
            logger.info("Gửi yêu cầu cập nhật số lượng sản phẩm tới: {}", updateProductUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                updateProductUrl,
                HttpMethod.PUT,
                entity,
                Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.warn("Cập nhật số lượng sản phẩm thất bại với status code: {}", 
                         response.getStatusCode());
                return false;
            }
            
            logger.info("Đã cập nhật số lượng sản phẩm ID {} thành công", productId);
            return true;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Lỗi khi gọi API cập nhật số lượng sản phẩm: status={}, body={}", 
                        e.getStatusCode(), e.getResponseBodyAsString(), e);
            return false;
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi cập nhật số lượng sản phẩm: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Khôi phục số lượng sản phẩm khi hủy đơn hàng
     */
    private boolean restoreProductQuantity(Long productId, Integer quantity) {
        if (productId == null || productId <= 0) {
            logger.warn("productId không hợp lệ: {}", productId);
            return false;
        }
        
        if (quantity == null || quantity <= 0) {
            logger.warn("quantity không hợp lệ: {}", quantity);
            return false;
        }
        
        try {
            // Sửa lại URL cho đúng với service discovery
            String restoreUrl = "http://product-service/api/products/" + productId 
                               + "/restoreQuantity?quantity=" + quantity;
            logger.info("Gửi yêu cầu khôi phục số lượng sản phẩm tới: {}", restoreUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                restoreUrl,
                HttpMethod.PUT,
                entity,
                Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.warn("Khôi phục số lượng sản phẩm thất bại với status code: {}", 
                         response.getStatusCode());
                return false;
            }
            
            logger.info("Đã khôi phục số lượng sản phẩm ID {} thành công", productId);
            return true;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Lỗi khi gọi API khôi phục số lượng sản phẩm: status={}, body={}", 
                        e.getStatusCode(), e.getResponseBodyAsString(), e);
            return false;
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi khôi phục số lượng sản phẩm: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy thông tin đơn hàng theo ID
     */
    public Order getOrderById(Long orderId) {
        if (orderId == null || orderId <= 0) {
            logger.warn("orderId không hợp lệ: {}", orderId);
            return null;
        }
        
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                logger.warn("Không tìm thấy đơn hàng với ID: {}", orderId);
                return null;
            }
            
            return orderOpt.get();
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm đơn hàng {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Không thể tìm kiếm đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách đơn hàng của người dùng
     */
    public List<Order> getOrdersByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            logger.warn("userId không hợp lệ: {}", userId);
            return List.of();
        }
        
        try {
            List<Order> orders = orderRepository.findByUserId(userId);
            logger.info("Tìm thấy {} đơn hàng cho người dùng ID {}", orders.size(), userId);
            return orders;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm đơn hàng của người dùng {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Không thể tìm kiếm đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật trạng thái đơn hàng
     */
    public Order updateOrderStatus(Long orderId, String status) {
        if (orderId == null || orderId <= 0) {
            logger.warn("orderId không hợp lệ: {}", orderId);
            return null;
        }
        
        if (status == null || status.isEmpty()) {
            logger.warn("status không hợp lệ: {}", status);
            return null;
        }
        
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                logger.warn("Không tìm thấy đơn hàng ID: {} để cập nhật trạng thái", orderId);
                return null;
            }
            
            Order order = orderOpt.get();
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            
            logger.info("Cập nhật trạng thái đơn hàng ID: {} thành {}", orderId, status);
            return orderRepository.save(order);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật trạng thái đơn hàng {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
    }
    
    /**
     * Hủy đơn hàng
     */
    public Order cancelOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            logger.warn("orderId không hợp lệ: {}", orderId);
            return null;
        }
        
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                logger.warn("Không tìm thấy đơn hàng ID: {} để hủy", orderId);
                return null;
            }
            
            Order order = orderOpt.get();
            
            // Kiểm tra nếu đơn hàng đã giao thì không thể hủy
            if ("DELIVERED".equals(order.getStatus())) {
                logger.warn("Không thể hủy đơn hàng ID: {} vì đã giao hàng", orderId);
                return null;
            }
            
            // Cập nhật trạng thái hủy
            order.setStatus("CANCELED");
            order.setUpdatedAt(LocalDateTime.now());
            
            // Khôi phục số lượng sản phẩm
            boolean restored = restoreProductQuantity(order.getProductId(), order.getQuantity());
            if (!restored) {
                logger.warn("Không thể khôi phục số lượng sản phẩm khi hủy đơn hàng ID {}", orderId);
            }
            
            logger.info("Đã hủy đơn hàng ID: {}", orderId);
            return orderRepository.save(order);
        } catch (Exception e) {
            logger.error("Lỗi khi hủy đơn hàng {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Không thể hủy đơn hàng: " + e.getMessage());
        }
    }
}