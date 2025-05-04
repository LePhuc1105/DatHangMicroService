package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    /**
     * Endpoint kiểm tra cấu trúc JSON
     */
    @PostMapping("/debug")
    public ResponseEntity<Object> debugOrderJson(@RequestBody Map<String, Object> orderData) {
        logger.info("Nhận dữ liệu debug raw JSON: {}", orderData);
        return ResponseEntity.ok(orderData);
    }

    /**
     * Tạo đơn hàng mới
     */
    @PostMapping
    public ResponseEntity<Object> createOrder(@RequestBody Order order) {
        logger.info("Nhận yêu cầu tạo đơn hàng mới: {}", order);
        logger.info("Chi tiết đơn hàng - productId: {}, userId: {}, quantity: {}, status: {}", 
            order.getProductId(), order.getUserId(), order.getQuantity(), order.getStatus());
        
        // Log any null fields for debugging
        if (order.getProductId() == null) logger.warn("productId là null");
        if (order.getQuantity() == null) logger.warn("quantity là null");
        if (order.getUserId() == null) logger.warn("userId là null");
        if (order.getStatus() == null) logger.warn("status là null");
        
        // Validate input data
        if (order.getProductId() == null || order.getQuantity() == null || 
            order.getQuantity() <= 0 || order.getUserId() == null || order.getUserId() <= 0) {
            
            logger.warn("Dữ liệu đơn hàng không hợp lệ: {}", order);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Dữ liệu đơn hàng không hợp lệ. Vui lòng kiểm tra lại thông tin!");
            return ResponseEntity.badRequest().body(response);
        }

        // Process the order
        Order createdOrder = orderService.createOrder(order);
        
        // Check the result and return appropriate response
        if (createdOrder == null) {
            logger.warn("Tạo đơn hàng thất bại do không đủ điều kiện");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Không thể tạo đơn hàng. Vui lòng kiểm tra thông tin khách hàng và số lượng sản phẩm!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        logger.info("Đơn hàng đã được tạo thành công với ID: {}", createdOrder.getId());
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "Đơn hàng đã được tạo thành công!");
        responseMap.put("id", createdOrder.getId());
        responseMap.put("order", createdOrder);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
    }

    /**
     * Lấy thông tin đơn hàng theo ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Object> getOrderById(@PathVariable Long orderId) {
        logger.info("Nhận yêu cầu lấy thông tin đơn hàng: {}", orderId);
        
        Order order = orderService.getOrderById(orderId);
        
        if (order == null) {
            logger.warn("Không tìm thấy đơn hàng với ID: {}", orderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Không tìm thấy đơn hàng với ID: " + orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        logger.info("Trả về thông tin đơn hàng: {}", orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Lấy danh sách đơn hàng của người dùng
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getUserOrders(@PathVariable Long userId) {
        logger.info("Nhận yêu cầu lấy danh sách đơn hàng của người dùng: {}", userId);
        
        List<Order> orders = orderService.getOrdersByUserId(userId);
        
        if (orders.isEmpty()) {
            logger.info("Không tìm thấy đơn hàng nào cho người dùng: {}", userId);
            return ResponseEntity.ok(orders); // Return empty array
        }
        
        logger.info("Trả về {} đơn hàng của người dùng: {}", orders.size(), userId);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Object> updateOrderStatus(
            @PathVariable Long orderId, 
            @RequestParam String status) {
        
        logger.info("Nhận yêu cầu cập nhật trạng thái đơn hàng: {}, status: {}", orderId, status);
        
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        
        if (updatedOrder == null) {
            logger.warn("Không thể cập nhật trạng thái đơn hàng ID: {}", orderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Không thể cập nhật trạng thái đơn hàng với ID: " + orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        logger.info("Đã cập nhật trạng thái đơn hàng ID: {} thành {}", orderId, status);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "Trạng thái đơn hàng đã được cập nhật thành công!");
        responseMap.put("order", updatedOrder);
        
        return ResponseEntity.ok(responseMap);
    }
    
    /**
     * Hủy đơn hàng
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Object> cancelOrder(@PathVariable Long orderId) {
        logger.info("Nhận yêu cầu hủy đơn hàng: {}", orderId);
        
        Order canceledOrder = orderService.cancelOrder(orderId);
        
        if (canceledOrder == null) {
            logger.warn("Không thể hủy đơn hàng ID: {}", orderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Không thể hủy đơn hàng với ID: " + orderId + ". Đơn hàng có thể không tồn tại hoặc đã được giao.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        logger.info("Đã hủy đơn hàng ID: {}", orderId);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", "Đơn hàng đã được hủy thành công!");
        responseMap.put("order", canceledOrder);
        
        return ResponseEntity.ok(responseMap);
    }
}
