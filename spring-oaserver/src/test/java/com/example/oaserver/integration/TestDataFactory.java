package com.example.oaserver.integration;

import com.example.oaserver.model.entity.Approval;
import com.example.oaserver.model.entity.Role;
import com.example.oaserver.model.entity.Todo;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.ApprovalStatus;
import com.example.oaserver.model.enums.ApprovalType;
import com.example.oaserver.model.enums.TodoPriority;
import com.example.oaserver.model.enums.RoleType;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * 测试数据工厂类，用于创建测试使用的实体对象
 */
public class TestDataFactory {

    /**
     * 创建管理员用户
     * @return User对象
     */
    public static User createAdminUser() {
        User admin = new User();
        admin.setId(UUID.randomUUID().toString());
        admin.setUsername("admin");
        admin.setPassword("$2a$10$eDzxFUeD1FB8Nw.AGZpSEOV5CimSrrA/DpnBN68LaTiQ0O8GV7BM2"); // 加密的"password"
        admin.setEmail("admin@example.com");
        admin.setName("Admin User");
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setRole("ADMIN");
        
        Set<String> roles = Set.of("ROLE_ADMIN");
        admin.setRoles(roles);
        return admin;
    }

    /**
     * 创建普通用户
     * @return User对象
     */
    public static User createRegularUser() {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername("user");
        user.setPassword("$2a$10$eDzxFUeD1FB8Nw.AGZpSEOV5CimSrrA/DpnBN68LaTiQ0O8GV7BM2"); // 加密的"password"
        user.setEmail("user@example.com");
        user.setName("Regular User");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole("USER");
        
        Set<String> roles = Set.of("ROLE_USER");
        user.setRoles(roles);
        return user;
    }

    /**
     * 创建测试待办事项
     * @param user 所属用户
     * @return Todo对象
     */
    public static Todo createTodo(User user) {
        Todo todo = new Todo();
        todo.setId(UUID.randomUUID().toString());
        todo.setTitle("Test Todo");
        todo.setDescription("This is a test todo item");
        todo.setCompleted(false);
        todo.setPriority(TodoPriority.MEDIUM);
        todo.setDueDate(LocalDateTime.now().plusDays(7));
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        todo.setUser(user);
        return todo;
    }

    /**
     * 创建测试审批请求
     * @param submitter 提交人
     * @param approver 审批人
     * @return Approval对象
     */
    public static Approval createApproval(User submitter, User approver) {
        Approval approval = new Approval();
        approval.setId(UUID.randomUUID().toString());
        approval.setTitle("Test Approval");
        approval.setDescription("This is a test approval request");
        approval.setType(ApprovalType.LEAVE);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setSubmitter(submitter);
        approval.setApprover(approver);
        approval.setCreatedAt(LocalDateTime.now());
        approval.setUpdatedAt(LocalDateTime.now());
        return approval;
    }
} 