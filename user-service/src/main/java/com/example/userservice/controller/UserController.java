package com.example.userservice.controller;

import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("User controller is working");
    }

    @GetMapping("/getInfo/{username}")
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // First check if user exists
            Optional<User> userOptional = userService.findByUsername(username);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(UserResponse.builder()
                        .username(username)
                        .fullName("User not found")
                        .build());
            }
            
            UserResponse userInfo = userService.getUserInfo(username);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
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
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        UserResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("User service is running");
    }
    
    @PutMapping("/updateInfo")
    public ResponseEntity<UserResponse> updateUserInfo(@RequestBody UserResponse userInfoUpdate) {
        String username = userInfoUpdate.getUsername();
        
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // Get existing user
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // Update user fields
                user.setFullName(userInfoUpdate.getFullName());
                user.setEmail(userInfoUpdate.getEmail());
                user.setPhone(userInfoUpdate.getPhone());
                user.setAddress(userInfoUpdate.getAddress());
                
                // Save updated user
                userService.saveUser(user);
                
                // Return updated user info
                UserResponse updatedInfo = userService.getUserInfo(username);
                return ResponseEntity.ok(updatedInfo);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/updateInfo")
    public ResponseEntity<UserResponse> updateUserInfoPost(@RequestBody UserResponse userInfoUpdate) {
        // Delegates to the PUT method implementation for convenience
        return updateUserInfo(userInfoUpdate);
    }

    // Debug endpoint to check if a user exists
    @GetMapping("/exists/{username}")
    public ResponseEntity<Map<String, Object>> checkUserExists(@PathVariable String username) {
        Optional<User> userOptional = userService.findByUsername(username);
        Map<String, Object> response = new HashMap<>();
        
        response.put("exists", userOptional.isPresent());
        response.put("username", username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test/createTestUser")
    public ResponseEntity<UserResponse> createTestUser() {
        try {
            // Check if test user already exists
            Optional<User> existingUser = userService.findByUsername("testuser");
            if (existingUser.isPresent()) {
                return ResponseEntity.ok(userService.getUserInfo("testuser"));
            }
            
            // Create test user registration request
            UserRegistrationRequest request = new UserRegistrationRequest();
            request.setUsername("testuser");
            request.setPassword("password");
            request.setEmail("test@example.com");
            request.setPhone("1234567890");
            request.setAddress("123 Test St");
            request.setFullName("Test User");
            
            // Register the user
            UserResponse response = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/test/updateTestUser")
    public ResponseEntity<String> updateTestUserForm() {
        return ResponseEntity.ok(
            "<html><body>" +
            "<h1>Update Test User</h1>" +
            "<form action='/api/users/updateInfo' method='post'>" +
            "  <input type='hidden' name='username' value='testuser'>" +
            "  <div>Username: testuser</div>" +
            "  <div>Full Name: <input type='text' name='fullName' value='Test User'></div>" +
            "  <div>Email: <input type='email' name='email' value='test@example.com'></div>" +
            "  <div>Phone: <input type='text' name='phone' value='1234567890'></div>" +
            "  <div>Address: <input type='text' name='address' value='123 Test St'></div>" +
            "  <div><button type='submit'>Update User</button></div>" +
            "</form>" +
            "</body></html>"
        );
    }

    // Add form data support
    @PostMapping(value = "/updateInfo", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<UserResponse> updateUserInfoForm(
            @RequestParam String username,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address) {
        
        // Create UserResponse object from form parameters
        UserResponse userInfoUpdate = UserResponse.builder()
                .username(username)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .address(address)
                .build();
        
        // Call the regular update method
        return updateUserInfo(userInfoUpdate);
    }
}
