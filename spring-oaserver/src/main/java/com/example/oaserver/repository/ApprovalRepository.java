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
    @Query("SELECT a FROM Approval a WHERE a.approver.id = ?1 AND a.status = ?2 ORDER BY a.createdAt DESC")
    List<Approval> findByApproverIdAndStatusOrderByCreatedAtDesc(String approverId, ApprovalStatus status);
    
    // 查找用户提交的申请
    @Query("SELECT a FROM Approval a WHERE a.submitter.id = ?1 ORDER BY a.createdAt DESC")
    List<Approval> findBySubmitterIdOrderByCreatedAtDesc(String submitterId);
    
    // 查找用户提交的指定类型的申请
    @Query("SELECT a FROM Approval a WHERE a.submitter.id = ?1 AND a.type = ?2 ORDER BY a.createdAt DESC")
    List<Approval> findBySubmitterIdAndTypeOrderByCreatedAtDesc(String submitterId, ApprovalType type);
    
    // 查找抄送给用户的审批
    @Query("SELECT a FROM Approval a WHERE ?1 MEMBER OF a.ccTo ORDER BY a.createdAt DESC")
    List<Approval> findCcToUser(String userId);
    
    // 查找指定类型和状态的审批
    @Query("SELECT a FROM Approval a WHERE a.type = ?1 AND a.status = ?2 ORDER BY a.createdAt DESC")
    List<Approval> findByTypeAndStatusOrderByCreatedAtDesc(ApprovalType type, ApprovalStatus status);
} 