package com.example.orderservice.service.impl;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OrderItemRepository;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        try {
            // 1. Validate order request
            validateOrderRequest(order);
            
            // 2. Verify user account
            verifyUserAccount(order.getCustomerUsername());
            
            // 3. Calculate total price and verify product availability
            double totalPrice = calculateTotalAndVerifyProducts(order);
            order.setTotalPrice(totalPrice);

            // 4. Save order with initial status
            Order savedOrder = saveInitialOrder(order);
            
            // 5. Try to update product inventory
            try {
                updateProductInventory(order);
            } catch (Exception e) {
                // Rollback already handled by @Transactional
                logger.error("Failed to update product inventory: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to update product inventory: " + e.getMessage());
            }
            
            // 6. Update order status to CONFIRMED after inventory update
            savedOrder.setStatus("CONFIRMED");
            savedOrder = orderRepository.save(savedOrder);
            
            // 7. Asynchronously process non-critical operations 
            // These operations should not affect the order placement transaction
            try {
                // Save customer information (non-critical)
                saveCustomerInfo(order);
                
                // Remove items from cart (non-critical)
                removeItemsFromCart(order);
                
                // Send confirmation email (non-critical)
                sendOrderConfirmationEmail(savedOrder);
            } catch (Exception e) {
                // Log but don't fail the transaction for non-critical operations
                logger.warn("Non-critical post-order operations failed: {}", e.getMessage(), e);
            }

            return savedOrder;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Business error creating order: {}", e.getMessage(), e);
            throw e;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("API error: {}", e.getMessage(), e);
            throw new RuntimeException("API error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating order: {}", e.getMessage(), e);
            throw new RuntimeException("Cannot create order: " + e.getMessage());
        }
    }
    
    private void validateOrderRequest(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            logger.warn("No products in order");
            throw new IllegalArgumentException("Order must contain at least one product");
        }
        
        if (order.getDeliveryDate() == null) {
            logger.warn("Delivery date is required");
            throw new IllegalArgumentException("Delivery date is required");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minDeliveryDate = now.plusDays(2);
        if (order.getDeliveryDate().isBefore(minDeliveryDate)) {
            logger.warn("Invalid delivery date: {}, must be after {}", 
                    order.getDeliveryDate(), minDeliveryDate);
            throw new IllegalArgumentException("Delivery date must be at least 2 days in the future");
        }
    }
    
    private void verifyUserAccount(String username) {
        String userPermissionUrl = "http://user-service:8083/api/users/" + username + "/permission";
        logger.debug("Sending permission verification request to: {}", userPermissionUrl);
        
        Boolean isUserValid = restTemplate.getForObject(userPermissionUrl, Boolean.class);
        if (isUserValid == null || !isUserValid) {
            logger.warn("Invalid account or insufficient permissions: {}", username);
            throw new IllegalArgumentException("Invalid account or insufficient permissions");
        }
    }
    
    private double calculateTotalAndVerifyProducts(Order order) {
        double totalPrice = 0.0;
        
        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null) {
                logger.warn("Found item with null productId");
                throw new IllegalArgumentException("productId cannot be null");
            }
            
            // Get product information
            String productUrl = "http://product-service:8082/api/products/" + item.getProductId();
            logger.debug("Fetching product information from: {}", productUrl);
            
            Map<String, Object> productResponse = restTemplate.getForObject(productUrl, Map.class);
            if (productResponse == null) {
                logger.warn("Product not found with ID: {}", item.getProductId());
                throw new IllegalArgumentException("Product does not exist: " + item.getProductId());
            }
            
            double productPrice = ((Number) productResponse.get("price")).doubleValue();
            item.setUnitPrice(productPrice);
            totalPrice += productPrice * item.getQuantity();
            
            // Verify product stock
            String productCheckUrl = "http://product-service:8082/api/products/check?productId=" + 
                    item.getProductId() + "&quantity=" + item.getQuantity();
            logger.debug("Verifying product stock: {}", productCheckUrl);
            
            Map<String, Boolean> productCheckResponse = restTemplate.getForObject(productCheckUrl, Map.class);
            if (productCheckResponse == null) {
                logger.error("No response from ProductService for productId: {}", item.getProductId());
                throw new RuntimeException("Error checking inventory for product: " + item.getProductId());
            }
            
            Boolean isProductAvailable = productCheckResponse.get("isAvailable");
            if (isProductAvailable == null || !isProductAvailable) {
                logger.warn("Product not available in requested quantity: {} (required: {})", 
                        item.getProductId(), item.getQuantity());
                throw new IllegalStateException("Product not available in requested quantity: " + item.getProductId());
            }
        }
        
        return totalPrice;
    }
    
    private Order saveInitialOrder(Order order) {
        // Set initial status and metadata
        order.setStatus("PROCESSING");
        order.setCreatedAt(LocalDateTime.now());
        
        logger.debug("Saving order to database: {}", order);
        Order savedOrder = orderRepository.save(order);
        
        // Save order items
        for (OrderItem item : order.getItems()) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);
        }
        
        return savedOrder;
    }
    
    private void updateProductInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            String updateProductUrl = "http://product-service:8082/api/products/" + 
                    item.getProductId() + "/updateQuantity?quantity=" + item.getQuantity();
            logger.debug("Updating product inventory: {}", updateProductUrl);
            
            restTemplate.put(updateProductUrl, null);
            logger.info("Successfully updated inventory for product ID: {}", item.getProductId());
        }
    }
    
    private void saveCustomerInfo(Order order) {
        String userInfoUrl = "http://user-service:8083/api/users/" + order.getCustomerUsername() + "/info";
        logger.debug("Saving customer information to: {}", userInfoUrl);
        
        restTemplate.postForEntity(userInfoUrl,
                Map.of(
                        "name", order.getCustomerName() != null ? order.getCustomerName() : "Unknown",
                        "address", order.getCustomerAddress() != null ? order.getCustomerAddress() : "Unknown",
                        "email", order.getCustomerEmail() != null ? order.getCustomerEmail() : "unknown@example.com",
                        "phone", order.getCustomerPhone() != null ? order.getCustomerPhone() : "Unknown"
                ),
                Void.class);
        logger.info("Successfully saved customer information for user: {}", order.getCustomerUsername());
    }
    
    private void removeItemsFromCart(Order order) {
        for (OrderItem item : order.getItems()) {
            String cartUrl = "http://cart-service:8084/api/cart/" + order.getCustomerUsername() + 
                    "/items/" + item.getProductId();
            logger.debug("Removing product from cart: {}", cartUrl);
            
            try {
                restTemplate.delete(cartUrl);
                logger.info("Successfully removed product from cart for user: {}", order.getCustomerUsername());
            } catch (Exception e) {
                logger.warn("Error removing product from cart, continuing process: {}", e.getMessage());
            }
        }
    }
    
    private void sendOrderConfirmationEmail(Order order) {
        String userInfoUrl = "http://user-service:8083/api/users/" + order.getCustomerUsername();
        Map<String, String> userInfo = restTemplate.getForObject(userInfoUrl, Map.class);
        String email = userInfo != null ? userInfo.get("email") : null;
        
        if (email != null) {
            try {
                String notificationUrl = "http://notification-service:8085/api/notifications/email";
                logger.debug("Sending confirmation email request to: {}", notificationUrl);
                
                String itemsJson = objectMapper.writeValueAsString(order.getItems());
                
                restTemplate.postForEntity(notificationUrl,
                        Map.of(
                                "email", email,
                                "orderId", order.getId(),
                                "status", order.getStatus(),
                                "items", itemsJson,
                                "totalPrice", order.getTotalPrice()
                        ),
                        Void.class);
                
                logger.info("Successfully sent confirmation email for order ID: {}", order.getId());
            } catch (Exception e) {
                logger.warn("Could not send confirmation email: {}", e.getMessage());
            }
        } else {
            logger.warn("No email found for user {}, skipping email notification", order.getCustomerUsername());
        }
    }

    @Override
    public Order getOrderById(Long orderId) {
        logger.debug("Finding order with ID: {}", orderId);
        try {
            return orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        } catch (IllegalArgumentException e) {
            logger.error("Error finding order {}: {}", orderId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error finding order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Cannot find order: " + e.getMessage());
        }
    }
}