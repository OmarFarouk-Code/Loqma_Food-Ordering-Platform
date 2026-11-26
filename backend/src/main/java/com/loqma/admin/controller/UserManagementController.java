package com.loqma.admin.controller;

import com.loqma.admin.dto.UserResponse;
import com.loqma.admin.service.UserManagementService;
import com.loqma.auth.entity.AccountStatus;
import com.loqma.auth.entity.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    // GET /api/admin/users or /api/admin/users?role=DRIVER&status=ACTIVE
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) AccountStatus status) {
        return ResponseEntity.ok(userManagementService.getUsers(role, status));
    }

    // GET /api/admin/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    // PATCH /api/admin/users/{id}/toggle-active
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<UserResponse> toggleUserActiveState(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.toggleUserActiveState(id));
    }
}
