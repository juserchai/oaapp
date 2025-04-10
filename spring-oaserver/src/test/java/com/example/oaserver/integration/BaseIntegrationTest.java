package com.example.oaserver.integration;

import com.example.oaserver.model.entity.User;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * 集成测试基类，提供通用测试功能和设置
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    protected User adminUser;
    protected User regularUser;
    protected String adminAuthToken;
    protected String userAuthToken;

    /**
     * 测试前设置，创建测试用户并生成认证令牌
     */
    @BeforeEach
    public void setUp() {
        // 清除之前的测试数据
        userRepository.deleteAll();
        
        // 创建测试用户
        adminUser = userRepository.save(TestDataFactory.createAdminUser());
        regularUser = userRepository.save(TestDataFactory.createRegularUser());
        
        // 生成认证令牌
        adminAuthToken = jwtTokenProvider.generateToken(adminUser.getUsername());
        userAuthToken = jwtTokenProvider.generateToken(regularUser.getUsername());
    }

    /**
     * 创建带有授权头的请求构建器
     * @param method HTTP方法
     * @param url API路径
     * @param token 认证令牌
     * @return MockHttpServletRequestBuilder
     */
    protected MockHttpServletRequestBuilder createAuthRequest(HttpMethod method, String url, String token) {
        MockHttpServletRequestBuilder requestBuilder;
        
        switch (method) {
            case GET:
                requestBuilder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url);
                break;
            case POST:
                requestBuilder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url);
                break;
            case PUT:
                requestBuilder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url);
                break;
            case DELETE:
                requestBuilder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(url);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        
        return requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token);
    }

    /**
     * 执行API请求并返回结果
     * @param requestBuilder 请求构建器
     * @param content 请求内容
     * @return ResultActions
     * @throws Exception 如果请求执行失败
     */
    protected ResultActions performRequest(MockHttpServletRequestBuilder requestBuilder, Object content) throws Exception {
        if (content != null) {
            requestBuilder.content(objectMapper.writeValueAsString(content));
        }
        return mockMvc.perform(requestBuilder);
    }

    /**
     * HTTP方法枚举
     */
    protected enum HttpMethod {
        GET, POST, PUT, DELETE
    }
} 