package com.chat_app.controller.authentication;

import com.chat_app.model.auth.ResponseModel;
import com.chat_app.model.auth.TokenModel;
import com.chat_app.service.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ResponseModel<?>> login(@RequestBody TokenModel user) {
            return authenticationService.verify(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseModel<?>> logout(@RequestHeader("Authorization") String authorization) {
        return authenticationService.logout(authorization);
    }

    @PostMapping("/forget-password")
    public ResponseEntity<ResponseModel<?>> passwordReset(@RequestBody Map<String, String>  email) {
        return authenticationService.sendResetPasswordLink(email.get("email"));
    }

    @PostMapping("/confirm-reset")
    public ResponseEntity<ResponseModel<?>> confirmPasswordReset(@RequestHeader("token") String token, @RequestBody Map<String, String> newPassword) {
        return authenticationService.resetPassword(token, newPassword.get("newPassword"));
    }
}