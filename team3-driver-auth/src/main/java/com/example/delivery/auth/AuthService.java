package com.example.delivery.auth;

import com.example.delivery.config.JwtService;
import com.example.delivery.user.AccountStatus;
import com.example.delivery.user.Role;
import com.example.delivery.user.User;
import com.example.delivery.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest req) {
        if (req.email() == null || req.email().isBlank() || req.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }
        if (req.role() == null || req.role() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }
        if (!isStrongPassword(req.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 12 characters and include a capital letter, a number and a symbol");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(req.role());
        user.setStatus(req.role() == Role.CUSTOMER ? AccountStatus.APPROVED : AccountStatus.PENDING);
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest req) {
        if (req.email() == null || req.password() == null) {
            throw invalidCredentials();
        }

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        switch (user.getStatus()) {
            case PENDING -> throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account is pending admin approval");
            case REJECTED -> throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account was rejected");
            case DEACTIVATED -> throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account has been deactivated");
            case APPROVED -> { }
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getRole().name());
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    private boolean isStrongPassword(String p) {
        return p.length() >= 12
                && p.matches(".*[A-Z].*")
                && p.matches(".*[0-9].*")
                && p.matches(".*[^A-Za-z0-9].*");
    }
}
