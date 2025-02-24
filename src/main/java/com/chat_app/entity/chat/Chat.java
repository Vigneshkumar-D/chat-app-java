package com.chat_app.entity.chat;

import com.chat_app.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;


@Entity
@Data
@Table(name = "chat")
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatName;

    private Boolean isGroup;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "chat_users",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "chat_admins", // New table for admins
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> admins;

    private Instant createdAt;
}
