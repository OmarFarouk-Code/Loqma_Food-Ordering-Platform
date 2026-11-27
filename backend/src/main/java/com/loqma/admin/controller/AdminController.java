package com.loqma.admin.controller;

import com.loqma.auth.entity.User;
import com.loqma.auth.repository.UserRepository;
import com.loqma.common.enums.AccountStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record RejectRequest(String reason) {}

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approve(@PathVariable String id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        user.setStatus(AccountStatus.ACTIVE);
        user.setRejectionReason(null);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Approved"));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> reject(@PathVariable String id,
                                                      @RequestBody RejectRequest req) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        user.setStatus(AccountStatus.REJECTED);
        user.setRejectionReason(req.reason());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Rejected"));
    }
}
