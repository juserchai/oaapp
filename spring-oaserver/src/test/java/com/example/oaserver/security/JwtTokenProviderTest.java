package com.example.oaserver.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test values for JWT properties
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "testSecretKeyThatIsLongEnoughForHS256Algorithm");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000); // 1 day
        
        // Initialize the secret key
        jwtTokenProvider.init();
        
        // Setup user details
        userDetails = new UserDetailsImpl(
            "user-id-123",
            "testuser",
            "password",
            "Test User",
            "test@example.com",
            "USER",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // When
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.").length).isEqualTo(3); // JWT has 3 parts
    }

    @Test
    void getUsernameFromJwtToken_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtTokenProvider.generateToken(authentication);
        
        // When
        String username = jwtTokenProvider.getUsernameFromJwtToken(token);
        
        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtTokenProvider.generateToken(authentication);
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.value";
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
} 