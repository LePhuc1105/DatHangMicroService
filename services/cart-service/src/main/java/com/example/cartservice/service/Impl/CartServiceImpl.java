package com.example.cartservice.service.impl;

import com.example.cartservice.entity.Cart;
import com.example.cartservice.entity.CartItem;
import com.example.cartservice.repository.CartItemRepository;
import com.example.cartservice.repository.CartRepository;
import com.example.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public void addProductToCart(String username, Long productId, int quantity) {
        logger.debug("Thêm sản phẩm {} vào giỏ hàng của người dùng: {}", productId, username);
        try {
            if (quantity <= 0) {
                logger.warn("Số lượng không hợp lệ: {}", quantity);
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }

            // Tìm hoặc tạo giỏ hàng
            Cart cart = getOrCreateCart(username);

            // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
            Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
            if (existingItem.isPresent()) {
                CartItem cartItem = existingItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                cartItemRepository.save(cartItem);
                logger.info("Cập nhật số lượng sản phẩm {} trong giỏ hàng của {}", productId, username);
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setCartId(cart.getId());
                cartItem.setProductId(productId);
                cartItem.setQuantity(quantity);
                cartItem.setCreatedAt(LocalDateTime.now());
                cartItemRepository.save(cartItem);
                logger.info("Thêm sản phẩm {} vào giỏ hàng của {}", productId, username);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi thêm sản phẩm vào giỏ hàng: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi thêm sản phẩm vào giỏ hàng: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể thêm sản phẩm vào giỏ hàng: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateProductQuantity(String username, Long productId, int quantity) {
        logger.debug("Cập nhật số lượng sản phẩm {} trong giỏ hàng của người dùng: {}", productId, username);
        try {
            if (quantity <= 0) {
                logger.warn("Số lượng không hợp lệ: {}", quantity);
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }

            Cart cart = getOrCreateCart(username);
            Optional<CartItem> cartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
            if (cartItemOptional.isPresent()) {
                CartItem cartItem = cartItemOptional.get();
                cartItem.setQuantity(quantity);
                cartItemRepository.save(cartItem);
                logger.info("Cập nhật số lượng sản phẩm {} thành {} trong giỏ hàng của {}", productId, quantity, username);
            } else {
                logger.warn("Sản phẩm {} không tồn tại trong giỏ hàng của {}", productId, username);
                throw new IllegalArgumentException("Sản phẩm không tồn tại trong giỏ hàng");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi cập nhật số lượng sản phẩm: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi cập nhật số lượng sản phẩm: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật số lượng sản phẩm: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void removeProductFromCart(String username, Long productId) {
        logger.debug("Xóa sản phẩm {} khỏi giỏ hàng của người dùng: {}", productId, username);
        try {
            Optional<Cart> cartOptional = cartRepository.findByUsername(username);
            if (cartOptional.isPresent()) {
                Cart cart = cartOptional.get();
                Optional<CartItem> cartItemOptional = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
                if (cartItemOptional.isPresent()) {
                    cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
                    logger.info("Xóa sản phẩm {} khỏi giỏ hàng của {}", productId, username);
                } else {
                    logger.warn("Sản phẩm {} không tồn tại trong giỏ hàng của {}", productId, username);
                    throw new IllegalArgumentException("Sản phẩm không tồn tại trong giỏ hàng");
                }
            } else {
                logger.warn("Không tìm thấy giỏ hàng cho người dùng: {}", username);
                throw new IllegalArgumentException("Không tìm thấy giỏ hàng");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi xóa sản phẩm khỏi giỏ hàng: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi xóa sản phẩm khỏi giỏ hàng: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xóa sản phẩm khỏi giỏ hàng: " + e.getMessage());
        }
    }

    @Override
    public List<CartItem> getCartItems(String username) {
        logger.debug("Lấy danh sách sản phẩm trong giỏ hàng của người dùng: {}", username);
        try {
            Optional<Cart> cartOptional = cartRepository.findByUsername(username);
            if (cartOptional.isPresent()) {
                return cartItemRepository.findByCartId(cartOptional.get().getId());
            } else {
                logger.warn("Không tìm thấy giỏ hàng cho người dùng: {}", username);
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách sản phẩm trong giỏ hàng: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách sản phẩm trong giỏ hàng: " + e.getMessage());
        }
    }

    private Cart getOrCreateCart(String username) {
        Optional<Cart> cartOptional = cartRepository.findByUsername(username);
        if (cartOptional.isPresent()) {
            return cartOptional.get();
        } else {
            Cart cart = new Cart();
            cart.setUsername(username);
            cart.setCreatedAt(LocalDateTime.now());
            return cartRepository.save(cart);
        }
    }
}