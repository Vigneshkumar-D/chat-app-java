package com.chat_app.dto;

import com.chat_app.entity.chat.Chat;
import com.chat_app.entity.chat.Message;
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
public class MessageDto {
    private String content;
    private LocalDateTime timestamp;
    private User sender;
    private Set<User> recipient;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    private Chat chat;

    public MessageDto(Message message) {
        this.content = message.getContent();
        this.timestamp = message.getTimestamp();
        this.sender = message.getSender();
        this.recipient = message.getRecipient();
        this.status = message.getStatus();
        this.chat = message.getChat();
    }
}
