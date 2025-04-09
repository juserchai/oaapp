package com.example.oaserver.repository;

import com.example.oaserver.model.entity.Todo;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.TodoPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, String> {
    
    List<Todo> findByUserOrderByCreatedAtDesc(User user);
    
    List<Todo> findByUserAndCompletedOrderByCreatedAtDesc(User user, boolean completed);
    
    @Query("SELECT t FROM Todo t WHERE t.user.id = ?1")
    List<Todo> findAllByUserId(String userId);
    
    @Query("SELECT t FROM Todo t WHERE t.id = ?1 AND t.user.id = ?2")
    Optional<Todo> findByIdAndUserId(String id, String userId);
    
    @Query("SELECT t FROM Todo t WHERE t.user = ?1 AND t.completed = false AND t.dueDate <= ?2 ORDER BY t.dueDate ASC")
    List<Todo> findOverdueTodos(User user, LocalDateTime now);
    
    @Query("SELECT t FROM Todo t WHERE t.user = ?1 AND t.completed = false AND t.dueDate BETWEEN ?2 AND ?3 ORDER BY t.dueDate ASC")
    List<Todo> findDueSoonTodos(User user, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT t FROM Todo t WHERE t.user.id = ?1 AND t.completed = ?2 ORDER BY t.createdAt DESC")
    List<Todo> findAllByUserIdAndCompletedOrderByCreatedAtDesc(String userId, boolean completed);
    
    @Query("SELECT t FROM Todo t WHERE t.user.id = ?1 AND t.priority = ?2 ORDER BY t.createdAt DESC")
    List<Todo> findAllByUserIdAndPriorityOrderByCreatedAtDesc(String userId, TodoPriority priority);
    
    @Query("SELECT t FROM Todo t WHERE t.user.id = ?1 AND t.dueDate <= ?2 AND t.completed = ?3 ORDER BY t.dueDate ASC")
    List<Todo> findAllByUserIdAndDueDateBeforeAndCompletedOrderByDueDateAsc(String userId, LocalDateTime dueDate, boolean completed);
} 