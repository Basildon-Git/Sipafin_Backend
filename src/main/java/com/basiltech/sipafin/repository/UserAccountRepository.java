package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.UserAccount;
import com.basiltech.sipafin.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    List<UserAccount> findByRole(UserRole role);

    List<UserAccount> findByEnabled(boolean enabled);

    List<UserAccount> findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String fullName,
            String username
    );
}