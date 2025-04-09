package com.example.oaserver.repository;

import com.example.oaserver.model.entity.Approval;
import com.example.oaserver.model.entity.User;
import com.example.oaserver.model.enums.ApprovalStatus;
import com.example.oaserver.model.enums.ApprovalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ApprovalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private UserRepository userRepository;

    private User requester;
    private User approver;
    private Approval approval1;
    private Approval approval2;

    @BeforeEach
    void setUp() {
        // Create test users
        requester = new User();
        requester.setUsername("requester");
        requester.setName("Test Requester");
        requester.setEmail("requester@example.com");
        requester.setPassword("password");
        requester.setEnabled(true);
        entityManager.persist(requester);

        approver = new User();
        approver.setUsername("approver");
        approver.setName("Test Approver");
        approver.setEmail("approver@example.com");
        approver.setPassword("password");
        approver.setEnabled(true);
        entityManager.persist(approver);

        // Create test approvals
        approval1 = new Approval();
        approval1.setTitle("Leave Request");
        approval1.setRequesterId(requester.getId());
        approval1.setApproverId(approver.getId());
        approval1.setType(ApprovalType.LEAVE);
        approval1.setStatus(ApprovalStatus.PENDING);
        entityManager.persist(approval1);

        approval2 = new Approval();
        approval2.setTitle("Expense Claim");
        approval2.setRequesterId(requester.getId());
        approval2.setApproverId(approver.getId());
        approval2.setType(ApprovalType.EXPENSE);
        approval2.setStatus(ApprovalStatus.PENDING);
        entityManager.persist(approval2);

        entityManager.flush();
    }

    @Test
    void testFindAllByRequesterId() {
        // When
        List<Approval> approvals = approvalRepository.findAllByRequesterId(requester.getId());

        // Then
        assertThat(approvals).hasSize(2);
        assertThat(approvals.get(0).getTitle()).isEqualTo("Leave Request");
        assertThat(approvals.get(1).getTitle()).isEqualTo("Expense Claim");
    }

    @Test
    void testFindAllByApproverId() {
        // When
        List<Approval> approvals = approvalRepository.findAllByApproverId(approver.getId());

        // Then
        assertThat(approvals).hasSize(2);
        assertThat(approvals.get(0).getTitle()).isEqualTo("Leave Request");
        assertThat(approvals.get(1).getTitle()).isEqualTo("Expense Claim");
    }

    @Test
    void testFindByIdAndApproverId() {
        // When
        Optional<Approval> foundApproval = approvalRepository.findByIdAndApproverId(approval1.getId(), approver.getId());

        // Then
        assertThat(foundApproval).isPresent();
        assertThat(foundApproval.get().getTitle()).isEqualTo("Leave Request");
    }

    @Test
    void testFindByApproverAndStatusOrderByCreatedAtDesc() {
        // When
        List<Approval> approvals = approvalRepository.findByApproverAndStatusOrderByCreatedAtDesc(
                approver, ApprovalStatus.PENDING);

        // Then
        assertThat(approvals).hasSize(2);
        assertThat(approvals.get(0).getTitle()).isEqualTo("Leave Request");
        assertThat(approvals.get(1).getTitle()).isEqualTo("Expense Claim");
    }

    @Test
    void testFindByTypeAndStatusOrderByCreatedAtDesc() {
        // When
        List<Approval> approvals = approvalRepository.findByTypeAndStatusOrderByCreatedAtDesc(
                ApprovalType.LEAVE, ApprovalStatus.PENDING);

        // Then
        assertThat(approvals).hasSize(1);
        assertThat(approvals.get(0).getTitle()).isEqualTo("Leave Request");
    }
} 