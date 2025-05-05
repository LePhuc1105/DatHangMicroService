package com.example.orderservice.service;

import com.example.orderservice.model.Order;

public interface OrderService {
    // Tạo đơn hàng mới
    Order createOrder(Order order);

    // Lấy thông tin đơn hàng theo ID
    Order getOrderById(Long orderId);
}