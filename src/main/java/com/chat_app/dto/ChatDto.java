package com.chat_app.dto;

import com.chat_app.entity.user.User;
import lombok.*;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatDto {
    private String chatName;
    private Boolean isGroup;
    private Set<User> admins;
    private Set<User> participants;
}
