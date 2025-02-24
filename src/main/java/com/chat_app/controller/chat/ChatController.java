package com.chat_app.controller.chat;

import com.chat_app.dto.ChatDto;
import com.chat_app.model.auth.ResponseModel;
import com.chat_app.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ResponseModel<?>> createChat(@RequestBody ChatDto chatDto) {
        return chatService.createChat(chatDto);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ResponseModel<?>> getChat(@PathVariable("chatId") Long chatId) {
        return chatService.getChatById(chatId);
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<ResponseModel<?>> getChatList(@PathVariable("userId") Long userId) {
        return chatService.getChatList(userId);
    }

    @PutMapping("/{chatId}")
    public ResponseEntity<ResponseModel<?>> updateChat(@RequestBody ChatDto chatDto, @PathVariable("chatId") Long chatId) {
        return chatService.updateChat(chatDto, chatId);
    }
}

