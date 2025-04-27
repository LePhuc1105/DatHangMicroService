package com.example.userservice.controller;

import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Kiểm tra tài khoản có đủ quyền hạn để đặt hàng
    @GetMapping("/{username}/check")
    public ResponseEntity<Boolean> checkUserPermissions(@PathVariable String username) {
        boolean hasPermission = userService.checkUserPermission(username);
        return ResponseEntity.ok(hasPermission);
    }
}
