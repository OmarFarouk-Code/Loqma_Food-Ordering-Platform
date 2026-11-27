package com.loqma.admin.dto;

import com.loqma.auth.entity.User;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;

public class UserResponse {

    private String id;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private AccountStatus status;
    private String rejectionReason;
    private int reapplicationAttempts;

    public static UserResponse fromEntity(User user) {
        UserResponse dto = new UserResponse();
        dto.id = user.getId();
        dto.fullName = user.getName();
        dto.email = user.getEmail();
        dto.phone = user.getPhone();
        dto.role = user.getRole();
        dto.status = user.getStatus();
        dto.rejectionReason = user.getRejectionReason();
        dto.reapplicationAttempts = user.getReapplicationAttempts();
        return dto;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public AccountStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public int getReapplicationAttempts() { return reapplicationAttempts; }
}
