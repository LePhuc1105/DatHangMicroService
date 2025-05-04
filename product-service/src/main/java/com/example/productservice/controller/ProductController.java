package com.example.productservice.controller;

import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    
    @GetMapping("/")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    // Lấy thông tin chi tiết sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Kiểm tra số lượng sản phẩm trong kho
    @GetMapping("/{id}/stock")
    public ResponseEntity<Integer> getProductStock(@PathVariable Long id) {
        int stock = productService.getProductStock(id);
        return ResponseEntity.ok(stock);
    }
    
    /**
     * API kiểm tra số lượng sản phẩm có đủ không
     * URL: /api/products/check?productId=1&quantity=1
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkProductStock(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        logger.info("Kiểm tra sản phẩm id={} với số lượng={}", productId, quantity);
        try {
            // Kiểm tra số lượng sản phẩm trong kho
            int stock = productService.getProductStock(productId);
            boolean isAvailable = stock >= quantity;

            // Trả về thông tin về số lượng sản phẩm và tình trạng đủ hay không
            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("availableStock", stock);
            response.put("requestedQuantity", quantity);
            response.put("isAvailable", isAvailable);

            logger.info("Kết quả kiểm tra: sản phẩm id={}, tồn kho={}, yêu cầu={}, đủ số lượng={}", 
                    productId, stock, quantity, isAvailable);
                    
            return ResponseEntity.ok(response); // Luôn trả về 200 OK với thông tin đủ hay không
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra sản phẩm: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi kiểm tra sản phẩm: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * API cập nhật số lượng sản phẩm sau khi đặt hàng
     * URL: /api/products/{id}/updateQuantity?quantity=1
     */
    @PutMapping("/{id}/updateQuantity")
    public ResponseEntity<Map<String, Object>> updateProductQuantity(
            @PathVariable Long id,
            @RequestParam int quantity) {
        logger.info("Cập nhật số lượng sản phẩm id={}, giảm đi={}", id, quantity);
        try {
            // Lấy số lượng hiện tại
            int currentStock = productService.getProductStock(id);
            
            // Kiểm tra nếu đủ số lượng
            if (currentStock < quantity) {
                logger.warn("Không đủ sản phẩm: id={}, tồn kho={}, yêu cầu={}", id, currentStock, quantity);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không đủ sản phẩm trong kho");
                response.put("productId", id);
                response.put("availableStock", currentStock);
                response.put("requestedQuantity", quantity);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Cập nhật số lượng sản phẩm
            productService.updateProductQuantity(id, quantity);
            
            // Lấy số lượng mới
            int newStock = productService.getProductStock(id);
            logger.info("Đã cập nhật sản phẩm id={}: số lượng trước={}, số lượng sau={}", 
                    id, currentStock, newStock);
                    
            // Trả về thông tin cập nhật
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productId", id);
            response.put("previousStock", currentStock);
            response.put("newStock", newStock);
            response.put("reducedBy", quantity);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Lỗi cập nhật số lượng sản phẩm: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi cập nhật số lượng sản phẩm: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
