package com.loqma.admin.repository;

import com.loqma.auth.entity.AccountStatus;
import com.loqma.auth.entity.User;
import com.loqma.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminUserRepository extends JpaRepository<User, UUID> {
    List<User> findByRole(UserRole role);
    List<User> findByStatus(AccountStatus status);
    List<User> findByRoleAndStatus(UserRole role, AccountStatus status);
}
