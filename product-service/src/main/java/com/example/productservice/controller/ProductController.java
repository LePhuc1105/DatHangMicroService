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
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkProductStock(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        try {
            // Kiểm tra số lượng sản phẩm trong kho
            int stock = productService.getProductStock(productId);
            boolean isAvailable = stock >= quantity;

            // Trả về thông tin về số lượng sản phẩm và tình trạng đủ hay không
            Map<String, Object> response = Map.of(
                    "productId", productId,
                    "availableStock", stock,
                    "isAvailable", isAvailable
            );

            if (isAvailable) {
                return ResponseEntity.ok(response); // Trả về 200 OK nếu sản phẩm đủ số lượng
            } else {
                return ResponseEntity.status(400).body(response); // Trả về 400 nếu số lượng không đủ
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi khi kiểm tra sản phẩm: " + e.getMessage()));
        }
    }

    // Cập nhật số lượng sản phẩm sau khi đặt hàng
    @PutMapping("/{id}/updateQuantity")
    public ResponseEntity<Void> updateProductQuantity(
            @PathVariable Long id,
            @RequestParam int quantity) {
        try {
            // Cập nhật số lượng sản phẩm
            productService.updateProductQuantity(id, quantity);
            return ResponseEntity.ok().build(); // Trả về 200 OK khi cập nhật thành công
        } catch (Exception e) {
            // Xử lý lỗi nếu có vấn đề trong quá trình cập nhật
            return ResponseEntity.status(500).build(); // Trả về 500 nếu có lỗi
        }
    }

}
