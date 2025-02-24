    package com.chat_app.entity.chat;

    import com.chat_app.entity.user.User;
    import com.chat_app.enums.MessageStatus;
    import jakarta.persistence.*;
    import lombok.*;

    import java.time.LocalDateTime;
    import java.util.Set;

    @Entity
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Message {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String content;
        private LocalDateTime timestamp;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "sender_id", nullable = false)
        private User sender;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "recipients", // New table for admins
                joinColumns = @JoinColumn(name = "message_id"),
                inverseJoinColumns = @JoinColumn(name = "user_id"))
        private Set<User> recipient;

        @Enumerated(EnumType.STRING)
        private MessageStatus status;

        @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
        @JoinColumn(name = "chat_id", nullable = false)
        private Chat chat;

    }
