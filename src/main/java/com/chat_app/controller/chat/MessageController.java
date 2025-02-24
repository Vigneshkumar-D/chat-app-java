package com.chat_app.controller.chat;

import com.chat_app.dto.MessageDto;
import com.chat_app.model.auth.ResponseModel;
import com.chat_app.service.chat.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/messages")
public class MessageController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDto messageDto) {
        messageService.sendMessage(messageDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<?>> getMessage(@PathVariable("chatId") Long id) {
        return messageService.getMessageById(id);
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<ResponseModel<?>> getMessagesByChat(@PathVariable("chatId") Long chatId) {
        return messageService.getMessagesByChatId(chatId);
    }

}