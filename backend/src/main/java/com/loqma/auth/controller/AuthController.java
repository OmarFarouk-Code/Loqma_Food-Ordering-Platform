package com.loqma.auth.controller;

import com.loqma.auth.dto.LoginRequest;
import com.loqma.auth.dto.LoginResponse;
import com.loqma.auth.dto.RegisterRequest;
import com.loqma.auth.service.AuthService;
import com.loqma.common.enums.AccountStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        return buildRegisterResponse(authService.register(request));
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> registerMultipart(
            @RequestPart("data") RegisterRequest request,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return buildRegisterResponse(authService.register(request, logo, coverImage));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication auth) {
        return ResponseEntity.ok(authService.me(String.valueOf(auth.getPrincipal())));
    }

    private ResponseEntity<Map<String, String>> buildRegisterResponse(AccountStatus status) {
        String message = status == AccountStatus.ACTIVE
                ? "Registered successfully"
                : "Registered successfully. Your account is pending admin approval";
        HttpStatus httpStatus = status == AccountStatus.ACTIVE
                ? HttpStatus.CREATED
                : HttpStatus.ACCEPTED;
        return ResponseEntity.status(httpStatus)
                .body(Map.of("message", message, "status", status.name()));
    }
}
