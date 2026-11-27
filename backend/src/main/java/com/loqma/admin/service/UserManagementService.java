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
public class UserManagementService {

    private final AdminUserRepository adminUserRepository;

    public UserManagementService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(UserRole role, AccountStatus status) {
        List<User> users;
        if (role != null && status != null) {
            users = adminUserRepository.findByRoleAndStatus(role, status);
        } else if (role != null) {
            users = adminUserRepository.findByRole(role);
        } else if (status != null) {
            users = adminUserRepository.findByStatus(status);
        } else {
            users = adminUserRepository.findAll();
        }
        return users.stream().map(UserResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse toggleUserActiveState(UUID userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getStatus() == AccountStatus.ACTIVE) {
            user.setStatus(AccountStatus.DEACTIVATED);
        } else if (user.getStatus() == AccountStatus.DEACTIVATED) {
            user.setStatus(AccountStatus.ACTIVE);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only ACTIVE or DEACTIVATED users can be toggled. Current status: " + user.getStatus()
            );
        }

        User saved = adminUserRepository.save(user);
        return UserResponse.fromEntity(saved);
    }
}
