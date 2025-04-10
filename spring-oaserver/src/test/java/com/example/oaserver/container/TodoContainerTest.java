package com.example.oaserver.container;

import com.example.oaserver.model.entity.Todo;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.TodoPriority;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testcontainers")
public class TodoContainerTest extends AbstractContainerBaseTest {

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

    private User testUser;
    private String userId;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        todoRepository.deleteAll();
        userRepository.deleteAll();
        
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole("USER");
        testUser.setEnabled(true);
        testUser.setName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
        userId = testUser.getId();
        
        // 设置认证请求头
        String authToken = jwtTokenProvider.generateToken(testUser.getUsername());
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
    }

    @Test
    void testCreateAndGetTodo() throws Exception {
        // 创建一个待办事项
        Map<String, Object> todoRequest = new HashMap<>();
        todoRequest.put("title", "测试待办");
        todoRequest.put("description", "这是一个测试待办事项");
        todoRequest.put("priority", TodoPriority.HIGH.name());
        todoRequest.put("dueDate", LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME));
        
        ResponseEntity<String> createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos",
                HttpMethod.POST,
                new HttpEntity<>(todoRequest, headers),
                String.class);
                
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        JsonNode root = objectMapper.readTree(createResponse.getBody());
        String todoId = root.path("id").asText();
        
        // 获取创建的待办事项并验证
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        JsonNode getTodo = objectMapper.readTree(getResponse.getBody());
        assertEquals("测试待办", getTodo.path("title").asText());
        assertEquals("这是一个测试待办事项", getTodo.path("description").asText());
        assertEquals(TodoPriority.HIGH.name(), getTodo.path("priority").asText());
    }
    
    @Test
    void testGetAllTodos() throws Exception {
        // 创建多个待办事项
        createTodo("待办1", "描述1", TodoPriority.HIGH);
        createTodo("待办2", "描述2", TodoPriority.MEDIUM);
        createTodo("待办3", "描述3", TodoPriority.LOW);
        
        // 获取所有待办事项
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode root = objectMapper.readTree(response.getBody());
        assertTrue(root.isArray());
        assertEquals(3, root.size());
    }
    
    @Test
    void testUpdateTodo() throws Exception {
        // 创建待办事项
        String todoId = createTodoAndGetId("原始待办", "原始描述", TodoPriority.MEDIUM);
        
        // 更新待办事项
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("title", "更新后的待办");
        updateRequest.put("description", "更新后的描述");
        updateRequest.put("priority", TodoPriority.HIGH.name());
        updateRequest.put("completed", true);
        
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, headers),
                String.class);
                
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        
        // 验证更新后的待办事项
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        JsonNode updatedTodo = objectMapper.readTree(getResponse.getBody());
        assertEquals("更新后的待办", updatedTodo.path("title").asText());
        assertEquals("更新后的描述", updatedTodo.path("description").asText());
        assertEquals(TodoPriority.HIGH.name(), updatedTodo.path("priority").asText());
        assertTrue(updatedTodo.path("completed").asBoolean());
    }
    
    @Test
    void testDeleteTodo() throws Exception {
        // 创建待办事项
        String todoId = createTodoAndGetId("待删除的待办", "要删除的描述", TodoPriority.LOW);
        
        // 删除待办事项
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        
        // 验证待办事项已删除
        ResponseEntity<String> getResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos/" + todoId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
                
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
    
    /**
     * 创建测试待办事项
     */
    private void createTodo(String title, String description, TodoPriority priority) {
        Map<String, Object> todoRequest = new HashMap<>();
        todoRequest.put("title", title);
        todoRequest.put("description", description);
        todoRequest.put("priority", priority.name());
        todoRequest.put("dueDate", LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME));
        
        restTemplate.exchange(
                "http://localhost:" + port + "/api/todos",
                HttpMethod.POST,
                new HttpEntity<>(todoRequest, headers),
                String.class);
    }
    
    /**
     * 创建测试待办事项并返回ID
     */
    private String createTodoAndGetId(String title, String description, TodoPriority priority) throws Exception {
        Map<String, Object> todoRequest = new HashMap<>();
        todoRequest.put("title", title);
        todoRequest.put("description", description);
        todoRequest.put("priority", priority.name());
        todoRequest.put("dueDate", LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME));
        
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/todos",
                HttpMethod.POST,
                new HttpEntity<>(todoRequest, headers),
                String.class);
                
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("id").asText();
    }
} 