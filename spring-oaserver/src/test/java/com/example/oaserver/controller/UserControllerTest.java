package com.example.oaserver.controller;

import com.example.oaserver.controller.request.ChangePasswordRequest;
import com.example.oaserver.controller.response.MessageResponse;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.security.UserDetailsImpl;
import com.example.oaserver.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    private UserDetailsImpl userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        // Create mock user
        userDetails = new UserDetailsImpl(
            "user1",             // id
            "testuser",          // username
            "password",          // password
            "Test User",         // name
            "test@example.com",  // email
            "USER",              // role
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
        );
        
        user = new User();
        user.setId("user1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setRole("USER");
        user.setPassword("encoded_password");
    }

    @Test
    void testGetUserProfile() throws Exception {
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        
        mockMvc.perform(get("/api/users/profile")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("user1")))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.role", is("USER")));
                
        verify(userRepository).findById("user1");
    }
    
    @Test
    void testGetUserProfileNotFound() throws Exception {
        when(userRepository.findById("user1")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/users/profile")
                .with(user(userDetails)))
                .andExpect(status().isNotFound());
                
        verify(userRepository).findById("user1");
    }
    
    @Test
    void testUpdateUserProfile() throws Exception {
        User updatedUser = new User();
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        mockMvc.perform(put("/api/users/profile")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Profile updated successfully")));
                
        verify(userRepository).findById("user1");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testUpdateUserProfileNotFound() throws Exception {
        User updatedUser = new User();
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");
        
        when(userRepository.findById("user1")).thenReturn(Optional.empty());
        
        mockMvc.perform(put("/api/users/profile")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound());
                
        verify(userRepository).findById("user1");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testChangePasswordSuccess() throws Exception {
        ChangePasswordRequest passwordRequest = new ChangePasswordRequest();
        passwordRequest.setCurrentPassword("current_password");
        passwordRequest.setNewPassword("new_password");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        
        when(userService.changePassword("user1", "current_password", "new_password")).thenReturn(true);
        
        mockMvc.perform(put("/api/users/password")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Password changed successfully")));
                
        verify(userService).changePassword("user1", "current_password", "new_password");
    }
    
    @Test
    void testChangePasswordFailure() throws Exception {
        ChangePasswordRequest passwordRequest = new ChangePasswordRequest();
        passwordRequest.setCurrentPassword("wrong_password");
        passwordRequest.setNewPassword("new_password");
        
        when(userService.changePassword("user1", "wrong_password", "new_password")).thenReturn(false);
        
        mockMvc.perform(put("/api/users/password")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Current password is incorrect")));
                
        verify(userService).changePassword("user1", "wrong_password", "new_password");
    }
} 