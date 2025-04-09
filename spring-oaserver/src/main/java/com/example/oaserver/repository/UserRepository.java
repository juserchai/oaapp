package com.example.oaserver.repository;

import com.example.oaserver.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
} 