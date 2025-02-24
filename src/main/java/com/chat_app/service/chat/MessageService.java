package com.chat_app.service.chat;

import com.chat_app.dto.MessageDto;
import com.chat_app.entity.chat.Chat;
import com.chat_app.entity.chat.Message;
import com.chat_app.entity.user.User;
import com.chat_app.enums.MessageStatus;
import com.chat_app.model.auth.ResponseModel;
import com.chat_app.repository.chat.ChatRepository;
import com.chat_app.repository.chat.MessageRepository;
import com.chat_app.repository.user.UserRepository;
import com.chat_app.service.user.UserService;
import com.chat_app.utils.ExceptionHandler.ExceptionHandlerUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ExceptionHandlerUtil exceptionHandlerUtil;

    @Transactional
    public void sendMessage(MessageDto messageDto) {
        try {
            User sender = userRepository.findById(messageDto.getSender().getId())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            Set<User> recipient = new HashSet<>();
            for(User user : messageDto.getRecipient()) {
                    User existingUser = userRepository.findById(user.getId())
                        .orElseThrow(() -> new RuntimeException("Recipient not found"));
                if(existingUser!=null) recipient.add(existingUser);
            }

            Chat chat = chatRepository.findById(messageDto.getChat().getId())
                    .orElseThrow(() -> new RuntimeException("Chat not found"));
            Message message = new Message();
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(MessageStatus.SENT);
            message.setChat(chat);
            message.setSender(sender);
            message.setRecipient(recipient);
            message.setContent(messageDto.getContent());
            Message savedMessage = messageRepository.save(message);

            if (messageDto.getChat().getIsGroup()) {
                messagingTemplate.convertAndSend(
                        "/topic/chat/group/" + messageDto.getChat().getId(),
                        savedMessage
                );
            } else {
                User user = messageDto.getRecipient().iterator().next();
                messagingTemplate.convertAndSend(
                        "/queue/message/" + user.getId() + messageDto.getChat().getId(),
                        savedMessage
                );
            }
        }catch (Exception e) {
            System.out.println("Error processing message "+ e);
            throw e;
        }

    }

    public ResponseEntity<ResponseModel<?>> getMessageById(Long id) {
        try{
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", messageRepository.findById(id)));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public ResponseEntity<ResponseModel<?>> getMessagesByChatId(Long chatId) {
        try{
            List<Message> messages = messageRepository.findByChatId(chatId);
            List<Message> updatedMessages = messages.stream()
                    .peek(message -> message.setStatus(MessageStatus.READ))
                    .collect(Collectors.toList());
            List<MessageDto> messageDtos = updatedMessages.stream()
                    .map(message -> new MessageDto(message))
                    .collect(Collectors.toList());
            messageRepository.saveAll(updatedMessages);

            return ResponseEntity.ok(new ResponseModel<>(true, "Success", messageDtos));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public ResponseEntity<ResponseModel<?>> getMessagesByUserId(Long userId) {
        try{
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", messageRepository.findBySenderIdOrRecipientId(userId)));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public ResponseEntity<ResponseModel<?>> searchMessages(Long chatId, String keyword) {
        try{
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", messageRepository.findByChatIdAndContentContainingIgnoreCase(chatId, keyword)));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public ResponseEntity<ResponseModel<?>> getLastMessageByChatId(Long chatId) {
        try{
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", messageRepository.findTopByChatIdOrderByTimestampDesc(chatId)));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

}