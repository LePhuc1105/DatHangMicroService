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

    // Tạo đơn hàng mới
    public Order createOrder(Order order) {
        try {
            // 1. Kiểm tra tài khoản người dùng
            String userServiceUrl = "http://user-service-new:8083/api/users/" + order.getCustomerName();
            logger.debug("Gửi yêu cầu kiểm tra tài khoản tới: {}", userServiceUrl);
            ResponseEntity<Map> userResponse = restTemplate.getForEntity(userServiceUrl, Map.class);
            boolean isUserValid = userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null;
            if (!isUserValid) {
                logger.warn("Tài khoản không hợp lệ: {}", order.getCustomerName());
                return null;
            }

            // 2. Kiểm tra sản phẩm có đủ số lượng không
            String productServiceUrl = "http://product-service:8082/api/products/check?productId=" + order.getProductId() + "&quantity=" + order.getQuantity();
            logger.debug("Gửi yêu cầu kiểm tra sản phẩm tới: {}", productServiceUrl);
            ResponseEntity<Map> productResponse = restTemplate.getForEntity(productServiceUrl, Map.class);
            Boolean isProductAvailable = productResponse.getBody() != null && (Boolean) productResponse.getBody().get("isAvailable");
            if (isProductAvailable == null || !isProductAvailable) {
                logger.warn("Sản phẩm không đủ số lượng: {} (số lượng yêu cầu: {})", order.getProductId(), order.getQuantity());
                return null;
            }


            // 3. Lưu đơn hàng vào cơ sở dữ liệu
            logger.debug("Lưu đơn hàng vào cơ sở dữ liệu: {}", order);
            Order savedOrder = orderRepository.save(order);

            // 4. Cập nhật lại số lượng sản phẩm sau khi đặt hàng
            String updateProductUrl = "http://product-service:8082/api/products/" + order.getProductId() + "/updateQuantity?quantity=" + order.getQuantity();
            logger.debug("Gửi yêu cầu cập nhật số lượng sản phẩm tới: {}", updateProductUrl);

            try {
                restTemplate.put(updateProductUrl, null); // Gửi yêu cầu PUT để cập nhật số lượng sản phẩm
                logger.info("Cập nhật số lượng sản phẩm thành công cho sản phẩm ID: {}", order.getProductId());
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                logger.error("Lỗi khi gọi API cập nhật số lượng sản phẩm: {}", e.getMessage(), e);
                throw new RuntimeException("Không thể cập nhật số lượng sản phẩm: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Lỗi không xác định khi cập nhật số lượng sản phẩm: {}", e.getMessage(), e);
                throw new RuntimeException("Không thể cập nhật số lượng sản phẩm: " + e.getMessage());
            }



            return savedOrder;
        } catch (HttpClientErrorException e) {
            logger.error("Lỗi khi gọi API: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            logger.error("Lỗi khi tạo đơn hàng: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo đơn hàng: " + e.getMessage());
        }
    }

    // Lấy thông tin đơn hàng theo ID
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