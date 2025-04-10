package com.example.oaserver.integration;

import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.Role;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserIntegrationTest {

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

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private String adminAuthToken;
    private HttpHeaders adminHeaders;
    private String regularAuthToken;
    private HttpHeaders regularHeaders;
    private String adminId;
    private String regularUserId;

    @BeforeEach
    void setUp() {
        // 设置MockMvc用于直接测试控制器
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 清理测试数据
        userRepository.deleteAll();
        
        // 创建管理员用户
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRole(Role.ADMIN);
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
        regularUser.setRole(Role.USER);
        regularUser.setEnabled(true);
        regularUser.setName("Regular User");
        regularUser.setCreatedAt(LocalDateTime.now());
        regularUser.setUpdatedAt(LocalDateTime.now());
        regularUser = userRepository.save(regularUser);
        regularUserId = regularUser.getId();
        
        // 获取管理员令牌和请求头
        adminAuthToken = generateAuthToken(adminId);
        adminHeaders = new HttpHeaders();
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);
        adminHeaders.setBearerAuth(adminAuthToken);
        
        // 获取普通用户令牌和请求头
        regularAuthToken = generateAuthToken(regularUserId);
        regularHeaders = new HttpHeaders();
        regularHeaders.setContentType(MediaType.APPLICATION_JSON);
        regularHeaders.setBearerAuth(regularAuthToken);
    }

    @Test
    void testGetCurrentUserProfile() throws Exception {
        // 获取当前用户信息
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/me",
                HttpMethod.GET,
                new HttpEntity<>(regularHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode root = objectMapper.readTree(response.getBody());
        assertEquals("SUCCESS", root.path("result").asText());
        assertEquals("user", root.path("data").path("username").asText());
        assertEquals("user@example.com", root.path("data").path("email").asText());
        assertEquals("USER", root.path("data").path("role").asText());
    }

    @Test
    void testAdminCanGetAllUsers() throws Exception {
        // 管理员获取所有用户
        ResponseEntity<String> adminResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(adminResponse.getBody());
        assertEquals("SUCCESS", root.path("result").asText());
        assertTrue(root.path("data").isArray());
        assertTrue(root.path("data").size() >= 2);
        
        // 普通用户无权限访问
        try {
            ResponseEntity<String> regularResponse = restTemplate.exchange(
                    "http://localhost:" + port + "/api/users",
                    HttpMethod.GET,
                    new HttpEntity<>(regularHeaders),
                    String.class);
                    
            // 确保返回了权限错误
            if (regularResponse.getStatusCode() != HttpStatus.FORBIDDEN) {
                fail("Expected 403 FORBIDDEN for unauthorized access, got: " + regularResponse.getStatusCode());
            }
        } catch (Exception e) {
            // 可能会抛异常也是一种合理的结果
            assertTrue(e.getMessage().contains("403") || e.getMessage().contains("Forbidden"));
        }
    }

    @Test
    void testAdminCanGetUserById() throws Exception {
        // 管理员获取指定用户
        ResponseEntity<String> adminResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + regularUserId,
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, adminResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(adminResponse.getBody());
        assertEquals("SUCCESS", root.path("result").asText());
        assertEquals("user", root.path("data").path("username").asText());
        
        // 普通用户无权限访问其他用户
        ResponseEntity<String> regularResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + adminId,
                HttpMethod.GET,
                new HttpEntity<>(regularHeaders),
                String.class);
                
        assertEquals(HttpStatus.FORBIDDEN, regularResponse.getStatusCode());
    }

    @Test
    void testCreateUser() throws Exception {
        // 管理员创建新用户
        Map<String, Object> newUserData = new HashMap<>();
        newUserData.put("username", "newuser");
        newUserData.put("email", "newuser@example.com");
        newUserData.put("password", "password");
        newUserData.put("name", "New User");
        newUserData.put("role", "USER");
        newUserData.put("enabled", true);
        
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users",
                HttpMethod.POST,
                new HttpEntity<>(newUserData, adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(createResponse.getBody());
        assertEquals("SUCCESS", root.path("result").asText());
        
        // 验证用户已创建 - 从响应中获取ID或使用用户名查询
        String username = root.path("data").path("username").asText();
        assertEquals("newuser", username);
        
        // 确认数据库中存在该用户
        Optional<User> createdUser = userRepository.findByUsername("newuser");
        assertTrue(createdUser.isPresent());
        assertEquals("newuser@example.com", createdUser.get().getEmail());
    }

    @Test
    void testUpdateUser() throws Exception {
        // 管理员更新用户信息
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("username", "updateduser");
        updateData.put("email", "updated@example.com");
        
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + regularUserId,
                HttpMethod.PUT,
                new HttpEntity<>(updateData, adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        
        // 验证用户已更新
        User updatedUser = userRepository.findById(regularUserId).orElseThrow();
        assertEquals("updateduser", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void testUserCanUpdateOwnProfile() throws Exception {
        // 用户更新自己的资料
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("username", "selfupdated");
        updateData.put("email", "selfupdated@example.com");
        
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/me",
                HttpMethod.PUT,
                new HttpEntity<>(updateData, regularHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        
        // 验证资料已更新
        User updatedUser = userRepository.findById(regularUserId).orElseThrow();
        assertEquals("selfupdated", updatedUser.getUsername());
        assertEquals("selfupdated@example.com", updatedUser.getEmail());
    }

    @Test
    void testChangePassword() throws Exception {
        // 用户修改密码
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("oldPassword", "password");
        passwordData.put("newPassword", "newpassword");
        
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/me/password",
                HttpMethod.PUT,
                new HttpEntity<>(passwordData, regularHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // 验证密码已修改
        User updatedUser = userRepository.findById(regularUserId).orElseThrow();
        assertTrue(passwordEncoder.matches("newpassword", updatedUser.getPassword()));
    }

    @Test
    void testDeleteUser() throws Exception {
        // 先确认用户存在
        assertTrue(userRepository.findById(regularUserId).isPresent());
        
        // 管理员删除用户
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/users/" + regularUserId,
                HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        
        // 验证用户已删除 - 注意可能设置了软删除
        Optional<User> deletedUser = userRepository.findById(regularUserId);
        if (deletedUser.isPresent()) {
            // 如果使用了软删除，检查enabled字段
            assertFalse(deletedUser.get().isEnabled());
        } else {
            // 如果是硬删除，确认用户不存在
            assertFalse(userRepository.findById(regularUserId).isPresent());
        }
    }

    // 生成测试用JWT令牌的辅助方法
    private String generateAuthToken(String userId) {
        return jwtTokenProvider.generateToken(userId);
    }
} 