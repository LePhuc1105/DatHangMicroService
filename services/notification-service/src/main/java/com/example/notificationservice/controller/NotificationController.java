package com.example.notificationservice.controller;

import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/email")
    public ResponseEntity<Void> sendOrderConfirmationEmail(@RequestBody EmailRequest request) {
        notificationService.sendOrderConfirmationEmail(
                request.getEmail(),
                request.getOrderId(),
                request.getStatus(),
                request.getItems(),
                request.getTotalPrice()
        );
        return ResponseEntity.ok().build();
    }

    // DTO để nhận thông tin từ request
    public static class EmailRequest {
        private String email;
        private Long orderId;
        private String status;
        private String items; // Danh sách items dưới dạng JSON
        private double totalPrice;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getItems() { return items; }
        public void setItems(String items) { this.items = items; }
        public double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    }
}