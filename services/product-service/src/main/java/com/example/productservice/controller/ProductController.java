package com.example.productservice.controller;

import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkProductAvailability(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        boolean isAvailable = productService.checkProductAvailability(productId, quantity);
        return ResponseEntity.ok(Map.of("isAvailable", isAvailable));
    }

    @PutMapping("/{id}/updateQuantity")
    public ResponseEntity<Void> updateProductQuantity(
            @PathVariable Long id,
            @RequestParam int quantity) {
        productService.updateProductQuantity(id, quantity);
        return ResponseEntity.ok().build();
    }
}