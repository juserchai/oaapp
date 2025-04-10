package com.example.oaserver.integration;

import com.example.oaserver.model.entity.Approval;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.ApprovalStatus;
import com.example.oaserver.model.enums.ApprovalType;
import com.example.oaserver.repository.ApprovalRepository;
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
@ActiveProfiles("test")
public class ApprovalIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String userAuthToken;
    private String managerId;
    private String userId;
    private HttpHeaders userHeaders;
    private HttpHeaders managerHeaders;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        approvalRepository.deleteAll();
        userRepository.deleteAll();
        
        // 创建管理员用户
        User manager = new User();
        manager.setUsername("manager");
        manager.setEmail("manager@example.com");
        manager.setPassword(passwordEncoder.encode("password"));
        manager.setRole("MANAGER");
        manager.setEnabled(true);
        manager.setName("Manager User");
        manager.setCreatedAt(LocalDateTime.now());
        manager.setUpdatedAt(LocalDateTime.now());
        manager = userRepository.save(manager);
        managerId = manager.getId();
        
        // 创建普通用户
        User user = new User();
        user.setUsername("employee");
        user.setEmail("employee@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole("USER");
        user.setEnabled(true);
        user.setName("Employee User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        userId = user.getId();
        
        // 设置用户请求头
        userAuthToken = jwtTokenProvider.generateToken(user.getUsername());
        userHeaders = new HttpHeaders();
        userHeaders.setContentType(MediaType.APPLICATION_JSON);
        userHeaders.setBearerAuth(userAuthToken);
        
        // 设置管理员请求头
        String managerAuthToken = jwtTokenProvider.generateToken(manager.getUsername());
        managerHeaders = new HttpHeaders();
        managerHeaders.setContentType(MediaType.APPLICATION_JSON);
        managerHeaders.setBearerAuth(managerAuthToken);
    }

    @Test
    void testApprovalLifecycle() throws Exception {
        // 1. 创建请假申请
        Map<String, Object> leaveRequest = new HashMap<>();
        leaveRequest.put("title", "请假申请");
        leaveRequest.put("description", "我需要请假3天");
        leaveRequest.put("type", ApprovalType.LEAVE.name());
        leaveRequest.put("approverId", managerId);
        
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/approvals",
                HttpMethod.POST,
                new HttpEntity<>(leaveRequest, userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(createResponse.getBody());
        assertEquals("SUCCESS", root.path("result").asText());
        String approvalId = root.path("data").path("id").asText();
        
        // 2. 验证请假申请状态是待审批
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/approvals/" + approvalId,
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        JsonNode getRoot = objectMapper.readTree(getResponse.getBody());
        assertEquals("PENDING", getRoot.path("data").path("status").asText());
        
        // 3. 经理审批通过
        ResponseEntity<String> approveResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/approvals/" + approvalId + "/approve",
                HttpMethod.PUT,
                new HttpEntity<>(managerHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, approveResponse.getStatusCode());
        
        // 4. 验证请假申请状态已更新为通过
        ResponseEntity<String> finalResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/approvals/" + approvalId,
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
        JsonNode finalRoot = objectMapper.readTree(finalResponse.getBody());
        assertEquals("APPROVED", finalRoot.path("data").path("status").asText());
    }
    
    @Test
    void testGetMyApprovals() throws Exception {
        // 创建多个申请
        createApproval("请假申请1", ApprovalType.LEAVE);
        createApproval("请假申请2", ApprovalType.LEAVE);
        createApproval("报销申请", ApprovalType.REIMBURSEMENT);
        
        // 获取用户的所有申请
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/approvals/submitted",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode root = objectMapper.readTree(response.getBody());
        assertEquals("SUCCESS", root.path("result").asText());
        assertTrue(root.path("data").isArray());
        assertEquals(3, root.path("data").size());
    }
    
    @Test
    void testGetPendingApprovals() throws Exception {
        // 创建待审批的申请
        createApproval("请假申请", ApprovalType.LEAVE);
        createApproval("报销申请", ApprovalType.REIMBURSEMENT);
        
        // 获取所有待审批的申请（管理员视图）
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/approvals/pending",
                HttpMethod.GET,
                new HttpEntity<>(managerHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode root = objectMapper.readTree(response.getBody());
        assertEquals("SUCCESS", root.path("result").asText());
        assertTrue(root.path("data").isArray());
        assertEquals(2, root.path("data").size());
    }
    
    @Test
    void testFilterApprovalsByType() throws Exception {
        // 创建不同类型的申请
        createApproval("请假申请", ApprovalType.LEAVE);
        createApproval("出差申请", ApprovalType.BUSINESS_TRIP);
        createApproval("报销申请", ApprovalType.REIMBURSEMENT);
        
        // 按类型过滤申请
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/approvals/submitted?type=LEAVE",
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                String.class);
                
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode root = objectMapper.readTree(response.getBody());
        assertEquals("SUCCESS", root.path("result").asText());
        assertTrue(root.path("data").isArray());
        assertEquals(1, root.path("data").size());
        assertEquals("请假申请", root.path("data").get(0).path("title").asText());
    }
    
    /**
     * 创建测试审批
     */
    private void createApproval(String title, ApprovalType type) {
        Map<String, Object> approvalRequest = new HashMap<>();
        approvalRequest.put("title", title);
        approvalRequest.put("description", "测试审批");
        approvalRequest.put("type", type.name());
        approvalRequest.put("approverId", managerId);
        
        restTemplate.exchange(
                "http://localhost:" + port + "/api/approvals",
                HttpMethod.POST,
                new HttpEntity<>(approvalRequest, userHeaders),
                String.class);
    }
    
    /**
     * 生成认证令牌
     */
    private String generateAuthToken(String userId) {
        return jwtTokenProvider.generateToken(userId);
    }
} 