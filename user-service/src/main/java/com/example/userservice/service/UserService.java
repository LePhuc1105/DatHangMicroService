package com.example.userservice.service;

import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    boolean checkUserPermission(String username);  // Phương thức kiểm tra quyền hạn người dùng
    
    // New methods for authentication
    UserResponse registerUser(UserRegistrationRequest request);
    UserResponse loginUser(LoginRequest request);
    
    // Get current user information
    UserResponse getUserInfo(String username);
    
    // Save user to repository
    User saveUser(User user);
}
