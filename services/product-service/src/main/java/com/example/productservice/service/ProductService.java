package com.example.productservice.service;

import com.example.productservice.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    // Tìm sản phẩm theo ID
    Optional<Product> findById(Long id);

    // Lấy danh sách tất cả sản phẩm
    List<Product> findAll();

    // Kiểm tra số lượng tồn kho
    int getProductStock(Long productId);

    // Kiểm tra xem sản phẩm có đủ số lượng hay không
    boolean checkProductAvailability(Long productId, int quantity);

    // Cập nhật số lượng tồn kho
    void updateProductQuantity(Long productId, int quantity);
}