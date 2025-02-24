package com.chat_app.model.chat;

import com.chat_app.entity.user.User;
import com.chat_app.enums.MessageStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatList {
    private Long id;
    private String chatName;
    private String lastMessage;
    private Boolean isGroup;
    private LocalDateTime lastMessageTime;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    private Integer unreadMessageCount;
    private User sender;
    private Set<User> participants;
    private Set<User> admins;

}
