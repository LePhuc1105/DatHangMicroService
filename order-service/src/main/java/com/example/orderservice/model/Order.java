package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "`order`")
@Getter
@Setter
@ToString
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    private Integer quantity;
    
    /**
     * Trạng thái đơn hàng: 
     * - PENDING: Đang chờ xử lý (mới tạo)
     * - CONFIRMED: Đã xác nhận
     * - SHIPPED: Đã gửi hàng
     * - DELIVERED: Đã giao hàng
     * - CANCELED: Đã hủy
     */
    @Column(name = "status")
    private String status = "PENDING";
    
    /**
     * Thời gian tạo đơn hàng
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Thời gian cập nhật đơn hàng gần nhất
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}