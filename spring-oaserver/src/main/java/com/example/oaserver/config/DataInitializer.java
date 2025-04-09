package com.example.oaserver.config;

import com.example.oaserver.model.entity.User;
import com.example.oaserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    CommandLineRunner initDatabase() {
        return args -> {
            log.info("Checking if admin user exists");
            if (!userRepository.existsByUsername("admin")) {
                log.info("Creating admin user");
                createAdminUser();
            }
            
            if (!userRepository.existsByUsername("user")) {
                log.info("Creating regular user");
                createRegularUser();
            }
        };
    }
    
    private void createAdminUser() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");
        
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password"))
                .name("Administrator")
                .email("admin@example.com")
                .role("ADMIN")
                .enabled(true)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        userRepository.save(admin);
    }
    
    private void createRegularUser() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        
        User user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .name("Regular User")
                .email("user@example.com")
                .role("USER")
                .enabled(true)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        userRepository.save(user);
    }
} 