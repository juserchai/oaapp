package com.example.oaserver.repository;

import com.example.oaserver.model.entity.Approval;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.ApprovalStatus;
import com.example.oaserver.model.enums.ApprovalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, String> {
    
    // 查找待用户审批的申请
    @Query("SELECT a FROM Approval a WHERE a.approverId = ?1 AND a.status = ?2 ORDER BY a.createdAt DESC")
    List<Approval> findByApproverAndStatusOrderByCreatedAtDesc(User approver, ApprovalStatus status);
    
    // 新增方法
    @Query("SELECT a FROM Approval a WHERE a.requesterId = ?1 ORDER BY a.createdAt DESC")
    List<Approval> findAllByRequesterId(String requesterId);
    
    @Query("SELECT a FROM Approval a WHERE a.approverId = ?1 ORDER BY a.createdAt DESC")
    List<Approval> findAllByApproverId(String approverId);
    
    @Query("SELECT a FROM Approval a WHERE a.id = ?1 AND a.approverId = ?2")
    Optional<Approval> findByIdAndApproverId(String id, String approverId);
    
    // 查找抄送给用户的审批
    @Query("SELECT a FROM Approval a WHERE ?1 MEMBER OF a.ccTo ORDER BY a.createdAt DESC")
    List<Approval> findCcToUser(User user);
    
    // 查找指定类型和状态的审批
    @Query("SELECT a FROM Approval a WHERE a.type = ?1 AND a.status = ?2 ORDER BY a.createdAt DESC")
    List<Approval> findByTypeAndStatusOrderByCreatedAtDesc(ApprovalType type, ApprovalStatus status);
} 