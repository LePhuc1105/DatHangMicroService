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
    
    @Column(name = "status")
    private String status = "PENDING";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
   
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}