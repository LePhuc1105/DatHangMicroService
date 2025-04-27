package com.example.userservice.service.impl;

import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Kiểm tra quyền của người dùng, ví dụ kiểm tra nếu user có quyền đặt hàng
    @Override
    public boolean checkUserPermission(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(u -> u.getUsername() != null).orElse(false);  // Kiểm tra người dùng có tồn tại hay không
    }
}
