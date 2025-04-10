package com.example.oaserver.container;

import com.example.oaserver.model.entity.User;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testcontainers")
public class UserContainerTest extends AbstractContainerBaseTest {
    
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminAuthToken;
    private HttpHeaders adminHeaders;
    private String userAuthToken;
    private HttpHeaders userHeaders;
    private String adminId;
    private String userId;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        userRepository.deleteAll();
        
        // 创建管理员用户
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRole("ADMIN");
        adminUser.setEnabled(true);
        adminUser.setName("Admin User");
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);
        adminId = adminUser.getId();
        
        // 创建普通用户
        User regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("password"));
        regularUser.setRole("USER");
        regularUser.setEnabled(true);
        regularUser.setName("Regular User");
        regularUser.setCreatedAt(LocalDateTime.now());
        regularUser.setUpdatedAt(LocalDateTime.now());
        regularUser = userRepository.save(regularUser);
        userId = regularUser.getId();
        
        // 设置管理员请求头
        adminAuthToken = jwtTokenProvider.generateToken(adminUser.getUsername());
        adminHeaders = new HttpHeaders();
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);
        adminHeaders.setBearerAuth(adminAuthToken);
        
        // 设置普通用户请求头
        userAuthToken = jwtTokenProvider.generateToken(regularUser.getUsername());
        userHeaders = new HttpHeaders();
        userHeaders.setContentType(MediaType.APPLICATION_JSON);
        userHeaders.setBearerAuth(userAuthToken);
    }

    @Test
    void testGetCurrentUserProfile() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/profile",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode root = objectMapper.readTree(response.getBody());
        assertEquals("user", root.path("username").asText());
        assertEquals("user@example.com", root.path("email").asText());
        assertEquals("Regular User", root.path("name").asText());
        assertEquals("USER", root.path("role").asText());
    }
    
    @Test
    void testAdminCanGetAllUsers() throws Exception {
        // 管理员能获取所有用户
        ResponseEntity<String> adminResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(adminResponse.getBody());
        assertTrue(root.isArray());
        assertEquals(2, root.size());
        
        // 普通用户不能获取所有用户
        ResponseEntity<String> userResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class);
                
        assertEquals(HttpStatus.FORBIDDEN, userResponse.getStatusCode());
    }
    
    @Test
    void testAdminCanGetUserById() throws Exception {
        // 管理员能获取指定用户
        ResponseEntity<String> adminResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(adminResponse.getBody());
        assertEquals("user", root.path("username").asText());
        
        // 管理员能获取自己的信息
        ResponseEntity<String> adminSelfResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + adminId,
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, adminSelfResponse.getStatusCode());
        
        // 普通用户不能获取其他用户信息
        ResponseEntity<String> userResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + adminId,
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class);
                
        assertEquals(HttpStatus.FORBIDDEN, userResponse.getStatusCode());
    }
    
    @Test
    void testCreateUser() throws Exception {
        // 管理员创建新用户
        Map<String, Object> newUserData = new HashMap<>();
        newUserData.put("username", "newuser");
        newUserData.put("email", "newuser@example.com");
        newUserData.put("password", "password123");
        newUserData.put("name", "New User");
        newUserData.put("role", "USER");
        
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users",
                HttpMethod.POST,
                new HttpEntity<>(newUserData, adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(createResponse.getBody());
        String newUserId = root.path("id").asText();
        
        // 验证用户已创建
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + newUserId,
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        JsonNode newUser = objectMapper.readTree(getResponse.getBody());
        assertEquals("newuser", newUser.path("username").asText());
        assertEquals("newuser@example.com", newUser.path("email").asText());
        assertEquals("New User", newUser.path("name").asText());
        assertEquals("USER", newUser.path("role").asText());
    }
    
    @Test
    void testUpdateUser() throws Exception {
        // 管理员更新用户信息
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Updated User Name");
        updateData.put("email", "updated@example.com");
        
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + userId,
                HttpMethod.PUT,
                new HttpEntity<>(updateData, adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        
        // 验证用户已更新
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        JsonNode updatedUser = objectMapper.readTree(getResponse.getBody());
        assertEquals("Updated User Name", updatedUser.path("name").asText());
        assertEquals("updated@example.com", updatedUser.path("email").asText());
    }
    
    @Test
    void testUserCanUpdateOwnProfile() throws Exception {
        // 用户更新自己的资料
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "My New Name");
        updateData.put("email", "mynew@example.com");
        
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/profile",
                HttpMethod.PUT,
                new HttpEntity<>(updateData, userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        
        // 验证资料已更新
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/profile",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        JsonNode updatedProfile = objectMapper.readTree(getResponse.getBody());
        assertEquals("My New Name", updatedProfile.path("name").asText());
        assertEquals("mynew@example.com", updatedProfile.path("email").asText());
    }
    
    @Test
    void testChangePassword() throws Exception {
        // 用户修改密码
        Map<String, Object> passwordRequest = new HashMap<>();
        passwordRequest.put("currentPassword", "password");
        passwordRequest.put("newPassword", "newPassword123");
        
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/password",
                HttpMethod.PUT,
                new HttpEntity<>(passwordRequest, userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // 使用旧令牌依然可以访问（因为令牌未过期）
        ResponseEntity<String> oldTokenResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/profile",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, oldTokenResponse.getStatusCode());
    }
    
    @Test
    void testDeleteUser() throws Exception {
        // 管理员删除用户
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        
        // 验证用户已删除
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
} 