package com.example.userservice.service.impl;

import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        logger.debug("Tìm kiếm người dùng với username: {}", username);
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm người dùng {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Không thể tìm kiếm người dùng: " + e.getMessage());
        }
    }

    @Override
    public boolean checkUserPermission(String username) {
        logger.debug("Kiểm tra quyền hạn cho người dùng: {}", username);
        try {
            Optional<User> user = userRepository.findByUsername(username);
            boolean hasPermission = user.isPresent() &&
                    user.get().getRole().equals("CUSTOMER") &&
                    user.get().isActive();
            if (!hasPermission) {
                logger.warn("Người dùng {} không có quyền đặt hàng hoặc không tồn tại", username);
            }
            return hasPermission;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra quyền hạn {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Không thể kiểm tra quyền hạn: " + e.getMessage());
        }
    }

    @Override
    public void saveUserInfo(String username, String name, String address, String email, String phone) {
        logger.debug("Lưu thông tin khách hàng cho username: {}", username);
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            User user;
            if (userOptional.isPresent()) {
                user = userOptional.get();
            } else {
                logger.warn("Không tìm thấy người dùng {}, tạo người dùng mới", username);
                user = new User();
                user.setUsername(username);
                user.setRole("CUSTOMER");
                user.setActive(true);
            }

            // Cập nhật thông tin
            user.setName(name);
            user.setAddress(address);
            user.setEmail(email);
            user.setPhone(phone);

            userRepository.save(user);
            logger.info("Lưu thông tin khách hàng thành công cho username: {}", username);
        } catch (Exception e) {
            logger.error("Lỗi khi lưu thông tin khách hàng {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Không thể lưu thông tin khách hàng: " + e.getMessage());
        }
    }
}