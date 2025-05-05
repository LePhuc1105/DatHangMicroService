package com.example.userservice.service;

import com.example.userservice.entity.User;

import java.util.Optional;

public interface UserService {
    // Tìm người dùng theo username
    Optional<User> findByUsername(String username);

    // Kiểm tra quyền hạn của người dùng
    boolean checkUserPermission(String username);

    // Lưu hoặc cập nhật thông tin khách hàng
    void saveUserInfo(String username, String name, String address, String email, String phone);
}