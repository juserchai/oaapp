package com.example.oaserver.repository;

import com.example.oaserver.model.entity.Todo;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.TodoPriority;
import com.example.oaserver.repository.TodoRepository;
import com.example.oaserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TodoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Todo todo1;
    private Todo todo2;
    private Todo todo3;

    @BeforeEach
    void setUp() {
        // Create a test user
        user = new User();
        user.setUsername("testuser");
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setEnabled(true);
        entityManager.persist(user);

        // Create test todos
        todo1 = new Todo();
        todo1.setTitle("Complete project");
        todo1.setDescription("Finish the OA project implementation");
        todo1.setPriority(TodoPriority.HIGH);
        todo1.setCompleted(false);
        todo1.setUser(user);
        todo1.setDueDate(LocalDateTime.now().plusDays(1));
        entityManager.persist(todo1);

        todo2 = new Todo();
        todo2.setTitle("Review code");
        todo2.setDescription("Review pull requests");
        todo2.setPriority(TodoPriority.MEDIUM);
        todo2.setCompleted(false);
        todo2.setUser(user);
        todo2.setDueDate(LocalDateTime.now().plusDays(2));
        entityManager.persist(todo2);

        todo3 = new Todo();
        todo3.setTitle("Write documentation");
        todo3.setDescription("Document the API");
        todo3.setPriority(TodoPriority.LOW);
        todo3.setCompleted(true);
        todo3.setUser(user);
        todo3.setCompletedAt(LocalDateTime.now().minusDays(1));
        entityManager.persist(todo3);

        entityManager.flush();
    }

    @Test
    void testFindAllByUserId() {
        // When
        List<Todo> todos = todoRepository.findAllByUserId(user.getId());

        // Then
        assertThat(todos).hasSize(3);
        assertThat(todos.get(0).getTitle()).isEqualTo("Complete project");
        assertThat(todos.get(1).getTitle()).isEqualTo("Review code");
        assertThat(todos.get(2).getTitle()).isEqualTo("Write documentation");
    }

    @Test
    void testFindByIdAndUserId() {
        // When
        Optional<Todo> foundTodo = todoRepository.findByIdAndUserId(todo1.getId(), user.getId());

        // Then
        assertThat(foundTodo).isPresent();
        assertThat(foundTodo.get().getTitle()).isEqualTo("Complete project");
    }

    @Test
    void testFindByIdAndUserIdNotFound() {
        // When
        Optional<Todo> foundTodo = todoRepository.findByIdAndUserId("non-existent-id", user.getId());

        // Then
        assertThat(foundTodo).isEmpty();
    }

    @Test
    void testFindAllByUserIdAndCompleted() {
        // When
        List<Todo> completedTodos = todoRepository.findAllByUserIdAndCompletedOrderByCreatedAtDesc(user.getId(), true);
        List<Todo> uncompletedTodos = todoRepository.findAllByUserIdAndCompletedOrderByCreatedAtDesc(user.getId(), false);

        // Then
        assertThat(completedTodos).hasSize(1);
        assertThat(completedTodos.get(0).getTitle()).isEqualTo("Write documentation");
        
        assertThat(uncompletedTodos).hasSize(2);
        assertThat(uncompletedTodos.get(0).getTitle()).isEqualTo("Complete project");
        assertThat(uncompletedTodos.get(1).getTitle()).isEqualTo("Review code");
    }

    @Test
    void testFindAllByUserIdAndPriority() {
        // When
        List<Todo> highPriorityTodos = todoRepository.findAllByUserIdAndPriorityOrderByCreatedAtDesc(user.getId(), TodoPriority.HIGH);
        List<Todo> mediumPriorityTodos = todoRepository.findAllByUserIdAndPriorityOrderByCreatedAtDesc(user.getId(), TodoPriority.MEDIUM);
        List<Todo> lowPriorityTodos = todoRepository.findAllByUserIdAndPriorityOrderByCreatedAtDesc(user.getId(), TodoPriority.LOW);

        // Then
        assertThat(highPriorityTodos).hasSize(1);
        assertThat(highPriorityTodos.get(0).getTitle()).isEqualTo("Complete project");
        
        assertThat(mediumPriorityTodos).hasSize(1);
        assertThat(mediumPriorityTodos.get(0).getTitle()).isEqualTo("Review code");
        
        assertThat(lowPriorityTodos).hasSize(1);
        assertThat(lowPriorityTodos.get(0).getTitle()).isEqualTo("Write documentation");
    }

    @Test
    void testFindAllByUserIdAndDueDateBefore() {
        // When
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(23).withMinute(59).withSecond(59);
        List<Todo> dueSoonTodos = todoRepository.findAllByUserIdAndDueDateBeforeAndCompletedOrderByDueDateAsc(
                user.getId(), tomorrow, false);

        // Then
        assertThat(dueSoonTodos).hasSize(1);
        assertThat(dueSoonTodos.get(0).getTitle()).isEqualTo("Complete project");
    }
} 