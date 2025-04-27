package com.example.productservice.service;

import com.example.productservice.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Optional<Product> findById(Long id);
    void updateProductQuantity(Long id, int quantity);
    int getProductStock(Long id);
    List<Product> findAll() ;

}
