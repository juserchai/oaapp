package com.example.oaserver.controller;

import com.example.oaserver.controller.response.MessageResponse;
import com.example.oaserver.model.entity.Approval;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.ApprovalStatus;
import com.example.oaserver.repository.ApprovalRepository;
import com.example.oaserver.repository.UserRepository;
import com.example.oaserver.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {
    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;

    @GetMapping("/submitted")
    public List<Approval> getSubmittedApprovals(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return approvalRepository.findBySubmitterIdOrderByCreatedAtDesc(userDetails.getId());
    }

    @GetMapping("/pending")
    public List<Approval> getPendingApprovals(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return approvalRepository.findByApproverIdAndStatusOrderByCreatedAtDesc(userDetails.getId(), ApprovalStatus.PENDING);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Approval> getApprovalById(@PathVariable String id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return approvalRepository.findById(id)
                .filter(approval -> approval.getSubmitter().getId().equals(userDetails.getId()) || 
                                   approval.getApprover().getId().equals(userDetails.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Approval createApproval(@Valid @RequestBody Map<String, Object> approvalRequest, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User submitter = userRepository.findById(userDetails.getId()).orElseThrow();
        User approver = userRepository.findById((String) approvalRequest.get("approverId")).orElseThrow();
        
        Approval approval = new Approval();
        approval.setTitle((String) approvalRequest.get("title"));
        approval.setDescription((String) approvalRequest.get("description"));
        approval.setType(com.example.oaserver.model.enums.ApprovalType.valueOf((String) approvalRequest.get("type")));
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setSubmitter(submitter);
        approval.setApprover(approver);
        
        return approvalRepository.save(approval);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable String id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return approvalRepository.findById(id)
                .filter(approval -> approval.getApprover().getId().equals(userDetails.getId()))
                .map(approval -> {
                    approval.setStatus(ApprovalStatus.APPROVED);
                    approvalRepository.save(approval);
                    return ResponseEntity.ok(new MessageResponse("Request approved successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable String id, @RequestParam String reason, 
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return approvalRepository.findById(id)
                .filter(approval -> approval.getApprover().getId().equals(userDetails.getId()))
                .map(approval -> {
                    approval.setStatus(ApprovalStatus.REJECTED);
                    approval.setRejectionReason(reason);
                    approvalRepository.save(approval);
                    return ResponseEntity.ok(new MessageResponse("Request rejected"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 