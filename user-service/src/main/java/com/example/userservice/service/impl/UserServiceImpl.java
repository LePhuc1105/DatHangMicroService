package com.example.userservice.service.impl;

import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Kiểm tra quyền của người dùng, ví dụ kiểm tra nếu user có quyền đặt hàng
    @Override
    public boolean checkUserPermission(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(u -> u.getUsername() != null && u.getActive()).orElse(false);
    }
    
    @Override
    public UserResponse registerUser(UserRegistrationRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        
        // Create new user
        User newUser = User.builder()
                .username(request.getUsername())
                .password(request.getPassword()) // In a real app, you should hash the password
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .fullName(request.getFullName())
                .role("USER")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Save user
        User savedUser = userRepository.save(newUser);
        
        // Generate a simple token (in a real app, use JWT or similar)
        String token = generateSimpleToken();
        
        // Return user response
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .address(savedUser.getAddress())
                .phone(savedUser.getPhone())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .token(token)
                .build();
    }
    
    @Override
    public UserResponse loginUser(LoginRequest request) {
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        
        // Check password (in a real app, verify hashed password)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        
        // Check if user is active
        if (!user.getActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate a simple token (in a real app, use JWT or similar)
        String token = generateSimpleToken();
        
        // Return user response
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .address(user.getAddress())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .role(user.getRole())
                .token(token)
                .build();
    }
    
    // Simple token generator (in a real app, use JWT with proper signing)
    private String generateSimpleToken() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public UserResponse getUserInfo(String username) {
        // Find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Return user response without token
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .address(user.getAddress())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
    
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
