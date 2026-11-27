package com.loqma.admin.repository;

import com.loqma.auth.entity.User;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminUserRepository extends JpaRepository<User, String> {
    List<User> findByRole(UserRole role);
    List<User> findByStatus(AccountStatus status);
    List<User> findByRoleAndStatus(UserRole role, AccountStatus status);
}
