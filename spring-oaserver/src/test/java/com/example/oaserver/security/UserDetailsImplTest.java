package com.example.oaserver.security;

import com.example.oaserver.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    @Test
    void build_ShouldCreateUserDetailsWithCorrectValues() {
        // Given
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");
        
        User user = User.builder()
                .id("user123")
                .username("testuser")
                .password("securepassword")
                .name("Test User")
                .email("test@example.com")
                .role("ADMIN")
                .roles(roles)
                .enabled(true)
                .build();
        
        // When
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        
        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getId()).isEqualTo("user123");
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("securepassword");
        assertThat(userDetails.getName()).isEqualTo("Test User");
        assertThat(userDetails.getEmail()).isEqualTo("test@example.com");
        assertThat(userDetails.getRole()).isEqualTo("ADMIN");
        
        // Check authorities
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(2);
        
        // Convert to strings and verify the expected values are present
        List<String> authorityStrings = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(authorityStrings).contains("ROLE_USER", "ROLE_ADMIN");
    }
    
    @Test
    void userDetailsDefaultMethods_ShouldReturnExpectedValues() {
        // Given
        UserDetailsImpl userDetails = new UserDetailsImpl(
                "user123",
                "testuser",
                "password",
                "Test User",
                "test@example.com",
                "USER",
                null
        );
        
        // Then - All these methods should return true by default as per implementation
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }
} 