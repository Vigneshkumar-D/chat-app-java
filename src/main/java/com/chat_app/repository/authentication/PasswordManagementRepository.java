package com.chat_app.repository.authentication;

import com.chat_app.entity.auth.PasswordManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordManagementRepository extends JpaRepository<PasswordManagement, Long> {
    Optional<PasswordManagement> findByToken(String token);
}
