package com.chat_app.service.authentication;

import com.chat_app.entity.auth.TokenBlacklist;
import com.chat_app.entity.auth.PasswordManagement;
import com.chat_app.entity.user.User;
import com.chat_app.model.auth.ResponseModel;
import com.chat_app.model.auth.TokenModel;
import com.chat_app.repository.authentication.PasswordManagementRepository;
import com.chat_app.repository.authentication.TokenBlacklistRepository;
import com.chat_app.repository.user.UserRepository;
import com.chat_app.utils.ExceptionHandler.ExceptionHandlerUtil;
import com.chat_app.utils.user.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private JWTUtil jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordManagementRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private ExceptionHandlerUtil exceptionHandlerUtil;

    @Autowired
    private JWTUtil jwtUtil;

    public ResponseEntity<ResponseModel<?>> verify(TokenModel user) {
        try{
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            if (authentication.isAuthenticated()) {
                Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
                User existingUser =  optionalUser.get();
                userRepository.save(existingUser);
                return ResponseEntity.ok(new ResponseModel<>(true, jwtService.generateToken(existingUser.getUsername()), 200));

            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseModel<>(false, "Authentication failed" , 500));
            }

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Authentication Error: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage()), 500));
        }
    }

    public ResponseEntity<ResponseModel<?>> logout(String authorization) {
        try{
            String jwtToken = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
            Date expirationDate = jwtUtil.extractExpiration(jwtToken);
            LocalDateTime localDateTime = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            TokenBlacklist blacklistedToken = new TokenBlacklist(jwtToken, localDateTime);
            tokenBlacklistRepository.save(blacklistedToken);
            return ResponseEntity.ok(new ResponseModel<>(true, "Logged out successfully!", 200));

        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error While logging out: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage()), 500));

        }

    }

    public ResponseEntity<ResponseModel<?>> sendResetPasswordLink(String email) {

        try{
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseModel<>(false, "User with email not found", 500));
            }
            User user = userOptional.get();

            String token = jwtUtil.generatePasswordResetToken(user.getUsername());
            PasswordManagement resetToken = new PasswordManagement();
            resetToken.setToken(token);
            resetToken.setExpiration(Instant.now().plusSeconds(600)); // Token valid for 1 hour
            resetToken.setUser(user);

            tokenRepository.save(resetToken);
            String rootUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "") + request.getContextPath();

            String body = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; background-color: #f9f9f9; color: #333; margin: 0; padding: 0; }" +
                    ".container { max-width: 600px; margin: 20px auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                    "h3 { color: #007bff; }" +
                    "p { font-size: 14px; line-height: 1.5; }" +
                    "a { color: #007bff; text-decoration: none; }" +
                    "a:hover { text-decoration: underline; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h3>Hello " + user.getUsername() + ",</h3>" +
                    "<p>You recently requested to reset your password for your ChatApp account. Click the button below to reset it:</p>" +
                    "<div style='text-align: center; margin: 20px 0;'>" +
                    "<a href='"+rootUrl+"/reset-password?token=" + token + "' style='background: #007bff; color: #fff; padding: 10px 20px; border-radius: 5px; font-size: 16px;'>Reset Your Password</a>" +
                    "</div>" +
                    "<p>If you did not request a password reset, please ignore this email or contact support if you have any concerns.</p>" +
                    "<p>This link is valid for 10 minutes.</p>" +
                    "<p>Regards,<br>Team ChatApp</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";


            emailService.sendEmail(user.getEmail(), "Password Reset", body );
            return ResponseEntity.ok(new ResponseModel<>(true, "A password reset link has been successfully sent to your registered email address.", 200));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error sending reset password link: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage()), 500));
        }


    }

    public ResponseEntity<ResponseModel<?>> resetPassword(String token, String newPassword) {
        try{
            PasswordManagement resetToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid or Expired link"));
            if (resetToken.isExpired()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseModel<>(false, "Link Expired", 500));
            }
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            tokenRepository.delete(resetToken);
            return ResponseEntity.ok(new ResponseModel<>(true, "Password successfully reset", 200));

        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseModel<>(false, "Error Resetting Password: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage()), 500));
        }

    }

}
