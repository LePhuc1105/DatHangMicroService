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

    @GetMapping("/{username}/permission")
    public ResponseEntity<Boolean> checkUserPermission(@PathVariable String username) {
        boolean hasPermission = userService.checkUserPermission(username);
        return ResponseEntity.ok(hasPermission);
    }

    @PostMapping("/{username}/info")
    public ResponseEntity<Void> saveUserInfo(
            @PathVariable String username,
            @RequestBody UserInfoRequest request) {
        userService.saveUserInfo(username, request.getName(), request.getAddress(),
                request.getEmail(), request.getPhone());
        return ResponseEntity.ok().build();
    }

    // DTO để nhận thông tin từ request
    public static class UserInfoRequest {
        private String name;
        private String address;
        private String email;
        private String phone;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}