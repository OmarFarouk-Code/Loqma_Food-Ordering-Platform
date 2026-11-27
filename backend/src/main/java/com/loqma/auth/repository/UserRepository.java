
package com.loqma.auth.repository;
import java.util.List;
import com.loqma.common.enums.AccountStatus;
import com.loqma.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByEmail(String email);
    List<User> findByStatus(AccountStatus status);
}