package com.example.oaserver.controller;

import com.example.oaserver.config.TestSecurityConfig;
import com.example.oaserver.controller.response.MessageResponse;
import com.example.oaserver.model.entity.Approval;
import com.example.oaserver.model.enums.ApprovalStatus;
import com.example.oaserver.model.enums.ApprovalType;
import com.example.oaserver.repository.ApprovalRepository;
import com.example.oaserver.security.AuthEntryPointJwt;
import com.example.oaserver.security.JwtTokenFilter;
import com.example.oaserver.security.JwtTokenProvider;
import com.example.oaserver.security.UserDetailsImpl;
import com.example.oaserver.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApprovalController.class)
@Import(TestSecurityConfig.class)
public class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovalRepository approvalRepository;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    @MockBean
    private JwtTokenFilter jwtTokenFilter;
    
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    
    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;
    
    @MockBean
    private AuthenticationManager authenticationManager;

    private UserDetailsImpl userDetails;
    private Approval approval1;
    private Approval approval2;
    private Authentication authentication;

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
        
        // Set up authentication
        authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Mock the UserDetailsService to return our userDetails
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        
        // Create test approvals with required fields that can't be null
        approval1 = new Approval();
        approval1.setId("approval1");
        approval1.setTitle("Leave Request");
        approval1.setRequesterId("user1");
        approval1.setApproverId("manager1");
        approval1.setStatus(ApprovalStatus.PENDING);
        approval1.setType(ApprovalType.LEAVE);
        approval1.setCreatedAt(LocalDateTime.now());
        approval1.setUpdatedAt(LocalDateTime.now());
        
        approval2 = new Approval();
        approval2.setId("approval2");
        approval2.setTitle("Expense Claim");
        approval2.setRequesterId("user1");
        approval2.setApproverId("manager1");
        approval2.setStatus(ApprovalStatus.PENDING);
        approval2.setType(ApprovalType.EXPENSE);
        approval2.setCreatedAt(LocalDateTime.now());
        approval2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testGetSubmittedApprovals() throws Exception {
        List<Approval> approvals = Arrays.asList(approval1, approval2);
        
        doReturn(approvals).when(approvalRepository).findAllByRequesterId(any());
        
        mockMvc.perform(get("/api/approvals/submitted")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetPendingApprovals() throws Exception {
        List<Approval> approvals = Arrays.asList(approval1, approval2);
        
        doReturn(approvals).when(approvalRepository).findAllByApproverId(any());
        
        mockMvc.perform(get("/api/approvals/pending")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetApprovalById() throws Exception {
        doReturn(Optional.of(approval1)).when(approvalRepository).findById("approval1");
        
        mockMvc.perform(get("/api/approvals/approval1")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("approval1"));
    }

    @Test
    void testCreateApproval() throws Exception {
        Approval newApproval = new Approval();
        newApproval.setTitle("Conference Attendance");
        newApproval.setApproverId("manager1");
        newApproval.setType(ApprovalType.TRAVEL);
        
        Approval savedApproval = new Approval();
        savedApproval.setId("approval3");
        savedApproval.setTitle("Conference Attendance");
        savedApproval.setRequesterId("user1");
        savedApproval.setApproverId("manager1");
        savedApproval.setStatus(ApprovalStatus.PENDING);
        savedApproval.setType(ApprovalType.TRAVEL);
        savedApproval.setCreatedAt(LocalDateTime.now());
        savedApproval.setUpdatedAt(LocalDateTime.now());
        
        doReturn(savedApproval).when(approvalRepository).save(any(Approval.class));
        
        mockMvc.perform(post("/api/approvals")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newApproval))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("approval3"));
    }

    @Test
    void testApproveRequest() throws Exception {
        doReturn(Optional.of(approval1)).when(approvalRepository).findByIdAndApproverId(eq("approval1"), any());
        
        mockMvc.perform(put("/api/approvals/approval1/approve")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Request approved successfully"));
    }

    @Test
    void testRejectRequest() throws Exception {
        doReturn(Optional.of(approval1)).when(approvalRepository).findByIdAndApproverId(eq("approval1"), any());
        
        mockMvc.perform(put("/api/approvals/approval1/reject")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .param("reason", "Insufficient information")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Request rejected"));
    }
}