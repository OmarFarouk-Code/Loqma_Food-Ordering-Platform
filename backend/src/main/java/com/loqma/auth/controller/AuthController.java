package com.loqma.auth.controller;

import com.loqma.auth.dto.LoginRequest;
import com.loqma.auth.dto.LoginResponse;
import com.loqma.auth.dto.RegisterRequest;
import com.loqma.auth.service.AuthService;
import com.loqma.common.enums.AccountStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        AccountStatus status = authService.register(request);
        String message = status == AccountStatus.ACTIVE
                ? "Registered successfully"
                : "Registered successfully. Your account is pending admin approval";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", message, "status", status.name()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication auth) {
        return ResponseEntity.ok(authService.me(String.valueOf(auth.getPrincipal())));
    }
}
