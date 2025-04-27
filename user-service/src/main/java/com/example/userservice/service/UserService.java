package com.example.userservice.service;

import com.example.userservice.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    boolean checkUserPermission(String username);  // Phương thức kiểm tra quyền hạn người dùng
}
