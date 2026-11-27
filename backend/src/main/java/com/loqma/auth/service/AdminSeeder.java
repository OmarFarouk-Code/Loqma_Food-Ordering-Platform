package com.loqma.auth.service;



import com.loqma.admin.entity.Admin;
import com.loqma.auth.repository.UserRepository;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String email;

    @Value("${app.admin.password:}")
    private String password;

    @Value("${app.admin.phone:01000000000}")
    private String phone;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(normalized).isPresent()) {
            return;
        }
        Admin admin = new Admin();
        admin.setName("Admin");
        admin.setEmail(normalized);
        admin.setPhone(phone);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);
        admin.setAdminCode("ADMIN-001");
        userRepository.save(admin);
    }
}