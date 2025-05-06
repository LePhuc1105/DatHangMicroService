package com.example.cartservice.service;

import com.example.cartservice.entity.CartItem;

import java.util.List;

public interface CartService {
    // Thêm sản phẩm vào giỏ hàng
    void addProductToCart(String username, Long productId, int quantity);

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    void updateProductQuantity(String username, Long productId, int quantity);

    // Xóa sản phẩm khỏi giỏ hàng
    void removeProductFromCart(String username, Long productId);

    // Lấy danh sách sản phẩm trong giỏ hàng
    List<CartItem> getCartItems(String username);
}