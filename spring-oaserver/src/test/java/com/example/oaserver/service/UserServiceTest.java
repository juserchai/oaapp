package com.example.oaserver;

import com.example.oaserver.exception.ResourceNotFoundException;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPassword("encodedPassword");
        user.setEnabled(true);
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        user.setRoles(roles);
    }

    @Test
    void testGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        verify(userRepository).findAll();
    }

    @Test
    void testGetUserById() {
        // Given
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserById("user1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findById("user1");
    }

    @Test
    void testGetUserByIdNotFound() {
        // Given
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.getUserById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("用户不存在");
        verify(userRepository).findById("nonexistent");
    }

    @Test
    void testGetUserByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserByUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("user1");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testGetUserByUsernameNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("用户不存在");
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void testCreateUser() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId("newUserId");
            return savedUser;
        });

        // When
        User result = userService.createUser(newUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("newUserId");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getRoles()).contains("ROLE_USER");
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUserUsernameExists() {
        // Given
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");
        
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> userService.createUser(newUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名已存在");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser() {
        // Given
        User userDetails = new User();
        userDetails.setName("Updated Name");
        userDetails.setEmail("updated@example.com");
        userDetails.setPhone("1234567890");
        userDetails.setDepartment("IT");
        userDetails.setPosition("Developer");
        userDetails.setAvatar("avatar.jpg");
        userDetails.setPassword("newPassword");
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.updateUser("user1", userDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getPhone()).isEqualTo("1234567890");
        assertThat(result.getDepartment()).isEqualTo("IT");
        assertThat(result.getPosition()).isEqualTo("Developer");
        assertThat(result.getAvatar()).isEqualTo("avatar.jpg");
        assertThat(result.getPassword()).isEqualTo("encodedNewPassword");
        verify(userRepository).findById("user1");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserNoPasswordChange() {
        // Given
        User userDetails = new User();
        userDetails.setName("Updated Name");
        userDetails.setEmail("updated@example.com");
        userDetails.setPassword(null);
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.updateUser("user1", userDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword"); // original password unchanged
        verify(userRepository).findById("user1");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(user);
    }

    @Test
    void testDeleteUser() {
        // Given
        when(userRepository.existsById("user1")).thenReturn(true);
        doNothing().when(userRepository).deleteById("user1");

        // When
        userService.deleteUser("user1");

        // Then
        verify(userRepository).existsById("user1");
        verify(userRepository).deleteById("user1");
    }

    @Test
    void testDeleteUserNotFound() {
        // Given
        when(userRepository.existsById("nonexistent")).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> userService.deleteUser("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("用户不存在");
        verify(userRepository).existsById("nonexistent");
        verify(userRepository, never()).deleteById(anyString());
    }

    @Test
    void testUpdateUserRoles() {
        // Given
        Set<String> newRoles = new HashSet<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.updateUserRoles("user1", newRoles);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRoles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        verify(userRepository).findById("user1");
        verify(userRepository).save(user);
    }

    @Test
    void testChangePasswordSuccess() {
        // Given
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        boolean result = userService.changePassword("user1", "currentPassword", "newPassword");

        // Then
        assertThat(result).isTrue();
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        verify(userRepository).findById("user1");
        verify(passwordEncoder).matches("currentPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
    }

    @Test
    void testChangePasswordFailure() {
        // Given
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When
        boolean result = userService.changePassword("user1", "wrongPassword", "newPassword");

        // Then
        assertThat(result).isFalse();
        assertThat(user.getPassword()).isEqualTo("encodedPassword"); // unchanged
        verify(userRepository).findById("user1");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
} 