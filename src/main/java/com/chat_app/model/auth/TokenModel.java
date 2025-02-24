package com.chat_app.model.auth;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TokenModel {
    private String username;
    private String password;
}
