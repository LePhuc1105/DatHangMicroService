package com.example.notificationservice.service;

public interface NotificationService {
    void sendOrderConfirmationEmail(String email, Long orderId, String status, String items, double totalPrice);
}