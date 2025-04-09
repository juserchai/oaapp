package com.example.oaserver.security;

import com.example.oaserver.model.entity.User;
import com.example.oaserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .id("user-id-123")
                .username("testuser")
                .password("password123")
                .name("Test User")
                .email("test@example.com")
                .role("USER")
                .roles(new HashSet<>())
                .enabled(true)
                .build();
        
        testUser.getRoles().add("ROLE_USER");
    }

    @Test
    void loadUserByUsername_WithExistingUsername_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(UserDetailsImpl.class);
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_WithNonExistingUsername_ShouldThrowException() {
        // Given
        String nonExistingUsername = "nonexistentuser";
        when(userRepository.findByUsername(nonExistingUsername)).thenReturn(Optional.empty());

        // When/Then
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistingUsername);
        });

        assertThat(exception.getMessage()).contains(nonExistingUsername);
        verify(userRepository, times(1)).findByUsername(nonExistingUsername);
    }
} 