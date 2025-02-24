package com.chat_app.service.user;


import com.chat_app.dto.UserDto;
import com.chat_app.entity.user.User;
import com.chat_app.model.auth.ResponseModel;
import com.chat_app.repository.chat.ChatRepository;
import com.chat_app.repository.user.UserRepository;
import com.chat_app.service.chat.ChatService;
import com.chat_app.utils.ExceptionHandler.ExceptionHandlerUtil;
//import com.chat_app.utils.user.JWTUtil;
import com.chat_app.utils.user.JWTUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ExceptionHandlerUtil exceptionHandlerUtil;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private JWTUtil jwtService;

    @Autowired
    private HttpServletRequest request;


    public ResponseEntity<ResponseModel<?>> getAllUsers() {
        try{
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", userRepository.findAll()));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public ResponseEntity<ResponseModel<?>> updateUser(Long userId, UserDto userDTO) {
        try{
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return null;
            }

            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            modelMapper.getConfiguration().setPropertyCondition(conditions -> {
                return conditions.getSource() != null;
            });
            modelMapper.map(userDTO, user);

            return ResponseEntity.ok(new ResponseModel<>(true, "Success", userRepository.save(user)));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error updating user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public ResponseEntity<ResponseModel<?>> createUser(UserDto userDTO) {
        try{
            User user = new User();
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            User savedUser = userRepository.save(user);
            chatService.createChatsForNewUser(savedUser);
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", savedUser));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error adding user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public ResponseEntity<ResponseModel<?>> getUserById(Long userId) {
        try{
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", userRepository.findById(userId)));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }

    }

    public ResponseEntity<ResponseModel<?>> deleteUser(Long userId){
        try {
            if (!userRepository.existsById(userId)) {
                // Return 404 Not Found
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseModel<>(false, "User not found"));
            }
            userRepository.deleteById(userId);
            // Return 200 OK if the category is deleted successfully
            return ResponseEntity.ok(new ResponseModel<>(true, "Deleted successfully"));
        } catch (Exception e) {
            // Return 500 Internal Server Error for any unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error deleting user: " + e.getMessage()));
        }
    }

    public String encodePassword(String password) {
        return this.passwordEncoder.encode(password);
    }

    public ResponseEntity<ResponseModel<?>> getCurrentUser() {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof String) {
                    return ResponseEntity.ok(new ResponseModel<>(true, "Success", userRepository.findByUsername((String) principal).get()));
                } else if (principal instanceof UserDetails) {
                    String username = ((UserDetails) principal).getUsername();
                    return ResponseEntity.ok(new ResponseModel<>(true, "Success", userRepository.findByUsername(username).get()));
                }
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving current user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
        return null;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }

    @PostConstruct
    public void addUsers() {
        List<User> users = Arrays.asList(
                new User(null,"John Doe", "john.doe@example.com", this.encodePassword("john.doe@123")),
                new User (null,"Vignesh",  "vigneshkumar.d2797@gmail.com", this.encodePassword("Vignesh@123")),
                new User(null,"Jane Smith", "jane.smith@example.com", this.encodePassword("jane.smith@123")),
                new User(null, "Alice Johnson", "alice.johnson@example.com", this.encodePassword("Alice@123")),
                new User(null, "Robert Brown", "robert.brown@example.com", this.encodePassword("Robert@123")),
                new User(null, "Emily Davis", "emily.davis@example.com", this.encodePassword("Emily@123")),
                new User(null, "Michael Wilson", "michael.wilson@example.com", this.encodePassword("Michael@123")),
                new User(null, "Sophia Martinez", "sophia.martinez@example.com", this.encodePassword("Sophia@123"))
        );

        List<User> savedUsers = new ArrayList<>();
        for (User user : users) {
            userRepository.findByUsername(user.getUsername())
                    .ifPresentOrElse(
                            existing -> System.out.println("User " + user.getUsername() + " already exists. "+ user.getPassword()),
                            () -> savedUsers.add(userRepository.save(user)));
        }
        if(!savedUsers.isEmpty()) chatService.createChatsForUsers(savedUsers);
    }
}
