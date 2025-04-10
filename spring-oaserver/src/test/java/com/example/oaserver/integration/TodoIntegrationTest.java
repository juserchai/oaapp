package com.example.oaserver.integration;

import com.example.oaserver.model.entity.Todo;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.Priority;
import com.example.oaserver.model.enums.Role;
import com.example.oaserver.repository.TodoRepository;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TodoIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private HttpHeaders headers;
    private String userId;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        todoRepository.deleteAll();
        userRepository.deleteAll();
        
        // 创建测试用户
        User user = new User();
        user.setUsername("todouser");
        user.setEmail("todouser@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setName("Todo User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        userId = user.getId();
        
        // 获取令牌
        authToken = generateAuthToken(userId);
        
        // 设置请求头
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
    }

    @Test
    void testTodoLifecycle() throws Exception {
        // 1. 创建待办事项
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", "完成项目报告");
        requestBody.put("description", "准备月度项目进度报告");
        requestBody.put("priority", Priority.HIGH.name());
        requestBody.put("dueDate", LocalDateTime.now().plusDays(5).toString());
        List<String> tags = Arrays.asList("报告", "项目");
        requestBody.put("tags", tags);
        requestBody.put("userId", userId); // 确保设置了用户ID

        ResponseEntity<String> createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos",
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class);

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        
        // 从响应中获取待办事项ID
        JsonNode root = objectMapper.readTree(createResponse.getBody());
        assertTrue(root.path("result").asText().equals("SUCCESS"));
        String todoId = root.path("data").path("id").asText();
        assertNotNull(todoId);
        
        // 2. 获取单个待办事项详情
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        root = objectMapper.readTree(getResponse.getBody());
        assertEquals("完成项目报告", root.path("data").path("title").asText());
        assertFalse(root.path("data").path("completed").asBoolean());
        
        // 3. 获取所有待办事项列表
        ResponseEntity<String> listResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        root = objectMapper.readTree(listResponse.getBody());
        assertTrue(root.path("data").isArray());
        assertTrue(root.path("data").size() >= 1);
        
        // 4. 更新待办事项
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("id", todoId);
        updateBody.put("title", "修订项目报告");
        updateBody.put("description", "根据反馈修订月度项目进度报告");
        updateBody.put("priority", Priority.MEDIUM.name());
        
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.PUT,
                new HttpEntity<>(updateBody, headers),
                String.class);
                
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        
        // 5. 验证更新生效
        ResponseEntity<String> checkUpdateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, checkUpdateResponse.getStatusCode());
        root = objectMapper.readTree(checkUpdateResponse.getBody());
        assertEquals("修订项目报告", root.path("data").path("title").asText());
        
        // 6. 完成待办事项
        Map<String, Object> completeBody = new HashMap<>();
        completeBody.put("completed", true);
        
        ResponseEntity<String> completeResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId + "/complete",
                HttpMethod.PATCH, // 可能需要根据API是PUT还是PATCH调整
                new HttpEntity<>(completeBody, headers),
                String.class);
                
        assertEquals(HttpStatus.OK, completeResponse.getStatusCode());
        
        // 7. 验证完成状态
        ResponseEntity<String> finalCheckResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, finalCheckResponse.getStatusCode());
        root = objectMapper.readTree(finalCheckResponse.getBody());
        assertTrue(root.path("data").path("completed").asBoolean());
        assertNotNull(root.path("data").path("completedAt").asText());
        
        // 8. 删除待办事项
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        
        // 9. 验证已删除 - 可能会返回404或者返回标记为删除状态的对象
        ResponseEntity<String> verifyDeleteResponse;
        try {
            verifyDeleteResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        
            // 如果使用软删除，可能会返回200但是有删除标记
            if (verifyDeleteResponse.getStatusCode() == HttpStatus.OK) {
                root = objectMapper.readTree(verifyDeleteResponse.getBody());
                // 检查是否标记为删除
                assertTrue(root.path("data").path("deleted").asBoolean(false));
            } else {
                // 否则应该返回404
                assertEquals(HttpStatus.NOT_FOUND, verifyDeleteResponse.getStatusCode());
            }
        } catch (Exception e) {
            // 如果抛出异常，确认是404错误
            assertTrue(e.getMessage().contains("404"));
        }
    }

    @Test
    void testFilterTodosByPriority() throws Exception {
        // 1. 创建多个不同优先级的待办事项
        String highId = createTodo("高优先级任务", "描述1", Priority.HIGH);
        String mediumId = createTodo("中优先级任务", "描述2", Priority.MEDIUM);
        String lowId = createTodo("低优先级任务", "描述3", Priority.LOW);
        
        // 2. 按高优先级筛选
        ResponseEntity<String> highPriorityResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos?priority=" + Priority.HIGH.name(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, highPriorityResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(highPriorityResponse.getBody());
        assertTrue(root.path("data").isArray());
        
        // 确认存在高优先级任务
        boolean foundHighPriorityTodo = false;
        for (JsonNode todoNode : root.path("data")) {
            if (todoNode.path("id").asText().equals(highId)) {
                foundHighPriorityTodo = true;
                assertEquals("高优先级任务", todoNode.path("title").asText());
                break;
            }
        }
        assertTrue(foundHighPriorityTodo, "未找到高优先级任务");
    }

    @Test
    void testFilterTodosByCompletionStatus() throws Exception {
        // 1. 创建待办事项
        String completedId = createTodo("已完成任务", "描述1", Priority.MEDIUM);
        String pendingId = createTodo("未完成任务", "描述2", Priority.MEDIUM);
        
        // 2. 将一个任务标记为已完成
        Map<String, Object> completeBody = new HashMap<>();
        completeBody.put("completed", true);
        
        restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + completedId + "/complete",
                HttpMethod.PUT,
                new HttpEntity<>(completeBody, headers),
                String.class);
        
        // 3. 筛选已完成待办事项
        ResponseEntity<String> completedResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos?completed=true",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, completedResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(completedResponse.getBody());
        assertTrue(root.path("data").isArray());
        assertEquals(1, root.path("data").size());
        assertEquals("已完成任务", root.path("data").get(0).path("title").asText());
        
        // 4. 筛选未完成待办事项
        ResponseEntity<String> pendingResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos?completed=false",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, pendingResponse.getStatusCode());
        root = objectMapper.readTree(pendingResponse.getBody());
        assertTrue(root.path("data").isArray());
        assertEquals(1, root.path("data").size());
        assertEquals("未完成任务", root.path("data").get(0).path("title").asText());
    }

    // 辅助方法：创建待办事项
    private String createTodo(String title, String description, Priority priority) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", title);
        requestBody.put("description", description);
        requestBody.put("priority", priority.name());
        requestBody.put("userId", userId);
        
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos",
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class);
        
        // 检查创建是否成功
        assertEquals(HttpStatus.OK, response.getStatusCode());
                
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("data").path("id").asText();
    }

    // 生成测试用JWT令牌的辅助方法
    private String generateAuthToken(String userId) {
        return jwtTokenProvider.generateToken(userId);
    }
} 