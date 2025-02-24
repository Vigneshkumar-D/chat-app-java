package com.chat_app.entity.auth;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private LocalDateTime expirationDate;

    public TokenBlacklist(String token, LocalDateTime expirationDate) {
        this.token = token;
        this.expirationDate = expirationDate;
    }

}
