package com.example.orderservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "`order`")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_address")
    private String customerAddress;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "product_id")
    private Long productId;

    private Integer quantity;
}