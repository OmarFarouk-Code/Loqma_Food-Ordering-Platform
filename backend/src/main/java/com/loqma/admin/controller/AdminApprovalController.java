package com.loqma.admin.controller;

import com.loqma.admin.dto.RejectApprovalRequest;
import com.loqma.admin.dto.UserResponse;
import com.loqma.admin.service.ApprovalService;
import com.loqma.auth.entity.UserRole;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/approvals")
public class AdminApprovalController {

    private final ApprovalService approvalService;

    public AdminApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public List<UserResponse> getPendingApprovals(@RequestParam(required = false) UserRole role) {
        return approvalService.getPendingApprovals(role);
    }

    @GetMapping("/{id}")
    public UserResponse getApprovalById(@PathVariable UUID id) {
        return approvalService.getApprovalById(id);
    }

    @PatchMapping("/{id}/approve")
    public UserResponse approveUser(@PathVariable UUID id) {
        return approvalService.approveUser(id);
    }

    @PatchMapping("/{id}/reject")
    public UserResponse rejectUser(
            @PathVariable UUID id,
            @Valid @RequestBody RejectApprovalRequest request
    ) {
        return approvalService.rejectUser(id, request.reason());
    }
}
