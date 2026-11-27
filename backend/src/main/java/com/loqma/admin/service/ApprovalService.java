package com.loqma.admin.service;

import com.loqma.admin.dto.UserResponse;
import com.loqma.admin.repository.AdminUserRepository;
import com.loqma.auth.entity.AccountStatus;
import com.loqma.auth.entity.User;
import com.loqma.auth.entity.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ApprovalService {

    private final AdminUserRepository adminUserRepository;

    public ApprovalService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingApprovals(UserRole role) {
        List<User> pendingUsers = (role != null)
                ? adminUserRepository.findByRoleAndStatus(role, AccountStatus.PENDING_APPROVAL)
                : adminUserRepository.findByStatus(AccountStatus.PENDING_APPROVAL);

        return pendingUsers.stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getApprovalById(UUID userId) {
        return UserResponse.fromEntity(findUserOrThrow(userId));
    }

    @Transactional
    public UserResponse approveUser(UUID userId) {
        User user = findUserOrThrow(userId);
        if (user.getStatus() != AccountStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PENDING_APPROVAL users can be approved"
            );
        }
        user.setStatus(AccountStatus.ACTIVE);
        user.setRejectionReason(null);
        return UserResponse.fromEntity(adminUserRepository.save(user));
    }

    @Transactional
    public UserResponse rejectUser(UUID userId, String reason) {
        User user = findUserOrThrow(userId);
        if (user.getStatus() != AccountStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PENDING_APPROVAL users can be rejected"
            );
        }
        user.setStatus(AccountStatus.REJECTED);
        user.setRejectionReason(reason.trim());
        user.setReapplicationAttempts(user.getReapplicationAttempts() + 1);
        return UserResponse.fromEntity(adminUserRepository.save(user));
    }

    private User findUserOrThrow(UUID userId) {
        return adminUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));
    }
}