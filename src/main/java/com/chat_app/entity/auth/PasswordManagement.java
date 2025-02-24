package com.chat_app.entity.auth;

import com.chat_app.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "password_management")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PasswordManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private Instant expiration;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isExpired() {
        return Instant.now().isAfter(expiration);
    }
}
