package com.example.orderservice.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_username", nullable = false)
    private String customerUsername;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Các trường tạm thời để lưu thông tin khách hàng từ client
    @Transient
    private String customerName;

    @Transient
    private String customerAddress;

    @Transient
    private String customerEmail;

    @Transient
    private String customerPhone;

    @Transient
    private List<OrderItem> items = new ArrayList<>();
}