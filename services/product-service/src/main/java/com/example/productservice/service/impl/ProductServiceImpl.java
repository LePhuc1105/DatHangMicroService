package com.example.productservice.service.impl;

import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;

    @Override
    public Optional<Product> findById(Long id) {
        logger.debug("Tìm kiếm sản phẩm với ID: {}", id);
        try {
            return productRepository.findById(id);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm sản phẩm {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Không thể tìm kiếm sản phẩm: " + e.getMessage());
        }
    }

    @Override
    public List<Product> findAll() {
        logger.debug("Lấy danh sách tất cả sản phẩm");
        try {
            return productRepository.findAll();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách sản phẩm: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    @Override
    public int getProductStock(Long productId) {
        logger.debug("Kiểm tra số lượng tồn kho cho sản phẩm ID: {}", productId);
        try {
            Optional<Product> product = productRepository.findById(productId);
            return product.map(Product::getQuantity).orElse(0);
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra tồn kho sản phẩm {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Không thể kiểm tra tồn kho: " + e.getMessage());
        }
    }

    @Override
    public boolean checkProductAvailability(Long productId, int quantity) {
        logger.debug("Kiểm tra tính khả dụng của sản phẩm ID: {} với số lượng: {}", productId, quantity);
        try {
            if (quantity <= 0) {
                logger.warn("Số lượng yêu cầu không hợp lệ: {}", quantity);
                return false;
            }
            Optional<Product> product = productRepository.findById(productId);
            boolean isAvailable = product.isPresent() && product.get().getQuantity() >= quantity;
            if (!isAvailable) {
                logger.warn("Sản phẩm ID: {} không đủ số lượng (yêu cầu: {}, tồn kho: {})",
                        productId, quantity, product.map(Product::getQuantity).orElse(0));
            }
            return isAvailable;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra tính khả dụng sản phẩm {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Không thể kiểm tra tính khả dụng: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateProductQuantity(Long productId, int quantity) {
        logger.debug("Cập nhật số lượng tồn kho cho sản phẩm ID: {} với số lượng: {}", productId, quantity);
        try {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                int newQuantity = product.getQuantity() - quantity;
                if (newQuantity >= 0) {
                    product.setQuantity(newQuantity);
                    productRepository.save(product);
                    logger.info("Cập nhật số lượng tồn kho thành công cho sản phẩm ID: {}", productId);
                } else {
                    logger.error("Số lượng sản phẩm không đủ trong kho: yêu cầu {}, tồn kho {}", quantity, product.getQuantity());
                    throw new IllegalArgumentException("Số lượng sản phẩm không đủ trong kho");
                }
            } else {
                logger.error("Không tìm thấy sản phẩm với ID: {}", productId);
                throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật số lượng tồn kho sản phẩm {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật số lượng tồn kho: " + e.getMessage());
        }
    }
}