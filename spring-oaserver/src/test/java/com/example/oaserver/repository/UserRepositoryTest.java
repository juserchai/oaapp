package com.example.oaserver.repository;

import com.example.oaserver.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = User.builder()
                .username("testuser")
                .password("password123")
                .name("Test User")
                .email("test@example.com")
                .role("USER")
                .enabled(true)
                .build();

        anotherUser = User.builder()
                .username("anotheruser")
                .password("password456")
                .name("Another User")
                .email("another@example.com")
                .role("USER")
                .enabled(true)
                .build();
    }

    @Test
    void testFindByUsername() {
        // Persist the user
        User persistedUser = entityManager.persistAndFlush(testUser);

        // Find by username
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(persistedUser.getId());
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void testFindByUsernameNotFound() {
        // Find by username that doesn't exist
        Optional<User> foundUser = userRepository.findByUsername("nonexistentuser");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testExistsByUsername() {
        // Persist the user
        entityManager.persistAndFlush(testUser);

        // Check if username exists
        boolean exists = userRepository.existsByUsername("testuser");
        boolean doesNotExist = userRepository.existsByUsername("nonexistentuser");

        // Assert
        assertThat(exists).isTrue();
        assertThat(doesNotExist).isFalse();
    }

    @Test
    void testFindByEmail() {
        // Persist the user
        User persistedUser = entityManager.persistAndFlush(testUser);

        // Find by email
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(persistedUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindByEmailNotFound() {
        // Find by email that doesn't exist
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testExistsByEmail() {
        // Persist the user
        entityManager.persistAndFlush(testUser);

        // Check if email exists
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean doesNotExist = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isTrue();
        assertThat(doesNotExist).isFalse();
    }

    @Test
    void testFindById() {
        // Persist the user
        User persistedUser = entityManager.persistAndFlush(testUser);

        // Find by id
        Optional<User> foundUser = userRepository.findById(persistedUser.getId());

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void testSaveUser() {
        // Save the user
        User savedUser = userRepository.save(testUser);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testUpdateUser() {
        // Persist the user
        User persistedUser = entityManager.persistAndFlush(testUser);

        // Update the user
        persistedUser.setName("Updated Name");
        persistedUser.setEmail("updated@example.com");
        User updatedUser = userRepository.save(persistedUser);

        // Assert
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testDeleteUser() {
        // Persist the user
        User persistedUser = entityManager.persistAndFlush(testUser);

        // Delete the user
        userRepository.delete(persistedUser);

        // Try to find the user
        Optional<User> foundUser = userRepository.findById(persistedUser.getId());

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testMultipleUsers() {
        // Persist multiple users
        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(anotherUser);

        // Count users
        long count = userRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }
} 