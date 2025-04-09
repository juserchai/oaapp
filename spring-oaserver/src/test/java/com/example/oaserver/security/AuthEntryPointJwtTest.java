package com.example.oaserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthEntryPointJwtTest {

    @InjectMocks
    private AuthEntryPointJwt authEntryPointJwt;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AuthenticationException authException;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authException = mock(AuthenticationException.class);
        
        request.setServletPath("/api/test");
        when(authException.getMessage()).thenReturn("Unauthorized access");
    }

    @Test
    void commence_ShouldReturnUnauthorizedResponseWithCorrectContent() throws IOException, ServletException {
        // When
        authEntryPointJwt.commence(request, response, authException);
        
        // Then
        // Verify response status and content type
        assertThat(response.getStatus()).isEqualTo(401); // HttpServletResponse.SC_UNAUTHORIZED
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        
        // Parse and verify response content
        Map<String, Object> responseBody = objectMapper.readValue(response.getContentAsString(), Map.class);
        
        assertThat(responseBody).containsEntry("status", 401);
        assertThat(responseBody).containsEntry("error", "Unauthorized");
        assertThat(responseBody).containsEntry("message", "Unauthorized access");
        assertThat(responseBody).containsEntry("path", "/api/test");
    }
} 