package com.example.oaserver.controller;

import com.example.oaserver.model.entity.Todo;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.TodoPriority;
import com.example.oaserver.repository.TodoRepository;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.security.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoRepository todoRepository;

    @MockBean
    private UserRepository userRepository;

    private UserDetailsImpl userDetails;
    private User user;
    private Todo todo1;
    private Todo todo2;

    @BeforeEach
    void setUp() {
        // Create a mock user
        userDetails = new UserDetailsImpl(
            "user1",             // id
            "testuser",          // username
            "password",          // password
            "Test User",         // name
            "test@example.com",  // email
            "USER",              // role
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
        );
        
        user = new User();
        user.setId("user1");
        user.setUsername("testuser");
        user.setName("Test User");
        
        // Create test todos
        todo1 = new Todo();
        todo1.setId("todo1");
        todo1.setTitle("Complete project");
        todo1.setDescription("Finish the OA project implementation");
        todo1.setPriority(TodoPriority.HIGH);
        todo1.setCompleted(false);
        todo1.setUser(user);
        
        todo2 = new Todo();
        todo2.setId("todo2");
        todo2.setTitle("Review code");
        todo2.setDescription("Review pull requests");
        todo2.setPriority(TodoPriority.MEDIUM);
        todo2.setCompleted(false);
        todo2.setUser(user);
    }

    @Test
    void testGetAllTodos() throws Exception {
        List<Todo> todos = Arrays.asList(todo1, todo2);
        
        when(todoRepository.findAllByUserId("user1")).thenReturn(todos);
        
        mockMvc.perform(get("/api/todos")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("todo1")))
                .andExpect(jsonPath("$[1].id", is("todo2")));
                
        verify(todoRepository).findAllByUserId("user1");
    }

    @Test
    void testGetTodoById() throws Exception {
        when(todoRepository.findByIdAndUserId("todo1", "user1")).thenReturn(Optional.of(todo1));
        
        mockMvc.perform(get("/api/todos/todo1")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("todo1")))
                .andExpect(jsonPath("$.title", is("Complete project")));
                
        verify(todoRepository).findByIdAndUserId("todo1", "user1");
    }

    @Test
    void testCreateTodo() throws Exception {
        Todo newTodo = new Todo();
        newTodo.setTitle("Test new API");
        newTodo.setDescription("Test the new API endpoints");
        newTodo.setPriority(TodoPriority.MEDIUM);
        newTodo.setCompleted(false);
        
        Todo savedTodo = new Todo();
        savedTodo.setId("todo3");
        savedTodo.setTitle("Test new API");
        savedTodo.setDescription("Test the new API endpoints");
        savedTodo.setPriority(TodoPriority.MEDIUM);
        savedTodo.setCompleted(false);
        savedTodo.setUser(user);
        
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        
        mockMvc.perform(post("/api/todos")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("todo3")))
                .andExpect(jsonPath("$.title", is("Test new API")));
                
        verify(userRepository).findById("user1");
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void testUpdateTodo() throws Exception {
        Todo todoUpdate = new Todo();
        todoUpdate.setTitle("Updated project");
        todoUpdate.setDescription("Updated description");
        todoUpdate.setPriority(TodoPriority.LOW);
        todoUpdate.setCompleted(true);
        
        Todo updatedTodo = new Todo();
        updatedTodo.setId("todo1");
        updatedTodo.setTitle("Updated project");
        updatedTodo.setDescription("Updated description");
        updatedTodo.setPriority(TodoPriority.LOW);
        updatedTodo.setCompleted(true);
        updatedTodo.setUser(user);
        
        when(todoRepository.findByIdAndUserId("todo1", "user1")).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);
        
        mockMvc.perform(put("/api/todos/todo1")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todoUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("todo1")))
                .andExpect(jsonPath("$.title", is("Updated project")))
                .andExpect(jsonPath("$.completed", is(true)));
                
        verify(todoRepository).findByIdAndUserId("todo1", "user1");
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void testDeleteTodo() throws Exception {
        when(todoRepository.findByIdAndUserId("todo1", "user1")).thenReturn(Optional.of(todo1));
        
        mockMvc.perform(delete("/api/todos/todo1")
                .with(user(userDetails)))
                .andExpect(status().isOk());
                
        verify(todoRepository).findByIdAndUserId("todo1", "user1");
        verify(todoRepository).delete(any(Todo.class));
    }
} 