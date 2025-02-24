package com.chat_app.controller.user;

import com.chat_app.dto.UserDto;
import com.chat_app.model.auth.ResponseModel;
import com.chat_app.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {


    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ResponseModel<?>> add(@RequestBody UserDto userDto){
       return userService.createUser(userDto);
    }

    @GetMapping
    public ResponseEntity<ResponseModel<?>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("current-user")
    public ResponseEntity<ResponseModel<?>> getCurrentUser(){
        return userService.getCurrentUser();
    }

}
