package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        // Kiểm tra tài khoản người dùng và tính hợp lệ đơn hàng
        Order createdOrder = orderService.createOrder(order);
        if (createdOrder == null) {
            return ResponseEntity.badRequest().build();  // Nếu có lỗi, trả về BadRequest
        }
        return ResponseEntity.ok(createdOrder);  // Trả về đơn hàng đã tạo
    }

    // Lấy thông tin đơn hàng theo ID
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();  // Nếu không tìm thấy đơn hàng, trả về NotFound
        }
        return ResponseEntity.ok(order);
    }
}
