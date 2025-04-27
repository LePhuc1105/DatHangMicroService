package com.example.productservice.service.impl;

import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }




    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }


    @Override
    public int getProductStock(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.map(Product::getQuantity).orElse(0); // Trả về 0 nếu sản phẩm không tồn tại
    }
    @Override
    public void updateProductQuantity(Long productId, int quantity) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            Product existingProduct = product.get();
            int newQuantity = existingProduct.getQuantity() - quantity;
            if (newQuantity >= 0) {
                existingProduct.setQuantity(newQuantity);
                productRepository.save(existingProduct);
            } else {
                throw new IllegalArgumentException("Số lượng sản phẩm không đủ trong kho");
            }
        } else {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId);
        }
    }


}
