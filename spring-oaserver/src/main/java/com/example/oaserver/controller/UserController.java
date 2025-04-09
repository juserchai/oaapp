package com.example.oaserver.controller;

import com.example.oaserver.controller.request.ChangePasswordRequest;
import com.example.oaserver.controller.response.MessageResponse;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.security.UserDetailsImpl;
import com.example.oaserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userRepository.findById(userDetails.getId())
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("name", user.getName());
                    response.put("role", user.getRole());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody User updatedUser,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userRepository.findById(userDetails.getId())
                .map(user -> {
                    user.setName(updatedUser.getName());
                    user.setEmail(updatedUser.getEmail());
                    userRepository.save(user);
                    return ResponseEntity.ok(new MessageResponse("Profile updated successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest passwordRequest,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean result = userService.changePassword(
                userDetails.getId(),
                passwordRequest.getCurrentPassword(),
                passwordRequest.getNewPassword()
        );

        if (result) {
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Current password is incorrect"));
        }
    }
} 