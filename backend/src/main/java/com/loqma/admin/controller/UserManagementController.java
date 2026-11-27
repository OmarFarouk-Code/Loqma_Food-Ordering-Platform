package com.loqma.admin.controller;

import com.loqma.admin.dto.UserResponse;
import com.loqma.admin.service.UserManagementService;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    // PATCH /api/admin/users/{id}/toggle-active
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<UserResponse> toggleUserActiveState(@PathVariable String id) {
        return ResponseEntity.ok(userManagementService.toggleUserActiveState(id));
    }
}
