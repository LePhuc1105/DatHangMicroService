package com.example.cartservice.controller;

import com.example.cartservice.entity.CartItem;
import com.example.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/{username}/items")
    public ResponseEntity<Void> addProductToCart(
            @PathVariable String username,
            @RequestBody CartItemRequest request) {
        cartService.addProductToCart(username, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/items/{productId}")
    public ResponseEntity<Void> updateProductQuantity(
            @PathVariable String username,
            @PathVariable Long productId,
            @RequestBody CartItemRequest request) {
        cartService.updateProductQuantity(username, productId, request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/items/{productId}")
    public ResponseEntity<Void> removeProductFromCart(
            @PathVariable String username,
            @PathVariable Long productId) {
        cartService.removeProductFromCart(username, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/items")
    public ResponseEntity<List<CartItem>> getCartItems(@PathVariable String username) {
        List<CartItem> items = cartService.getCartItems(username);
        return ResponseEntity.ok(items);
    }

    // DTO để nhận thông tin từ request
    public static class CartItemRequest {
        private Long productId;
        private int quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}