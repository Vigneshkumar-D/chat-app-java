package com.chat_app.repository.authentication;

import com.chat_app.entity.auth.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    Boolean existsByToken(String token);
}
