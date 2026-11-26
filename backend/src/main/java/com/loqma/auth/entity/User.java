package com.loqma.auth.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.PENDING_APPROVAL;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "reapplication_attempts", nullable = false)
    private int reapplicationAttempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public int getReapplicationAttempts() { return reapplicationAttempts; }
    public void setReapplicationAttempts(int reapplicationAttempts) { this.reapplicationAttempts = reapplicationAttempts; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

