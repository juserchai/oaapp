package com.example.oaserver.controller;

import com.example.oaserver.model.entity.Todo;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.repository.TodoRepository;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Todo> getAllTodos(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return todoRepository.findAllByUserId(userDetails.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable String id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return todoRepository.findByIdAndUserId(id, userDetails.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Todo createTodo(@Valid @RequestBody Todo todo, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable String id, @Valid @RequestBody Todo todoDetails,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return todoRepository.findByIdAndUserId(id, userDetails.getId())
                .map(todo -> {
                    todo.setTitle(todoDetails.getTitle());
                    todo.setDescription(todoDetails.getDescription());
                    todo.setDueDate(todoDetails.getDueDate());
                    todo.setPriority(todoDetails.getPriority());
                    todo.setCompleted(todoDetails.isCompleted());
                    return ResponseEntity.ok(todoRepository.save(todo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable String id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return todoRepository.findByIdAndUserId(id, userDetails.getId())
                .map(todo -> {
                    todoRepository.delete(todo);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 