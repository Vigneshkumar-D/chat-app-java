package com.chat_app.service.chat;

import com.chat_app.dto.ChatDto;
import com.chat_app.entity.chat.Chat;
import com.chat_app.entity.chat.Message;
import com.chat_app.entity.user.User;
import com.chat_app.enums.MessageStatus;
import com.chat_app.model.chat.ChatList;
import com.chat_app.model.auth.ResponseModel;
import com.chat_app.repository.chat.ChatRepository;
import com.chat_app.repository.chat.MessageRepository;
import com.chat_app.repository.user.UserRepository;
import com.chat_app.utils.ExceptionHandler.ExceptionHandlerUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExceptionHandlerUtil exceptionHandlerUtil;

    public ResponseEntity<ResponseModel<?>> getChatList(Long userId) {
        try {

            List<Chat> existingChats = chatRepository.findByParticipants_Id(userId);
            List<User> otherUsers = userRepository.findAllByIdNot(userId);
            User currentUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

            if (existingChats.isEmpty()) {
                for (User otherUser : otherUsers) {
                    // Check if a chat between these users already exists
                    Optional<Chat> existingChat = chatRepository.findByParticipants(currentUser.getId(), otherUser.getId());
                    if (existingChat.isEmpty()) {
                        Chat chat = new Chat();
                        chat.setIsGroup(false);
                        chat.setParticipants(Set.of(currentUser, otherUser));
                        chat.setCreatedAt(Instant.now());
                        chatRepository.save(chat);
                    }
                }

                // Re-fetch chats after creating them
                existingChats = chatRepository.findByParticipants_Id(userId);
            }

            List<ChatList> chatLists = existingChats.stream()
                    .map(chat -> {
                        Optional<Message> lastMessageOpt = messageRepository.findLastMessageByChatId(chat.getId());
                        Message lastMessage = lastMessageOpt.orElse(null);
                        String chatName = chat.getChatName();

                        if (!chat.getIsGroup()) {
                            chatName = chat.getParticipants().stream()
                                    .filter(user -> !user.getId().equals(userId)) // Excluding current user to set dynamic chat name
                                    .findFirst()
                                    .map(User::getUsername)
                                    .orElse("Unknown Chat");
                        }

                        return new ChatList(
                                chat.getId() != null ? chat.getId() : 0L,
                                chatName,
                                lastMessage != null ? lastMessage.getContent() : "No messages yet",
                                chat.getIsGroup(),
                                lastMessage != null && lastMessage.getTimestamp() != null ?
                                        lastMessage.getTimestamp() : null,
                                lastMessage != null ? MessageStatus.DELIVERED : MessageStatus.SENT,
                                this.calculateUnreadMessages(chat.getId(), userId),
                                lastMessage != null ? lastMessage.getSender() : null,
                                chat.getParticipants(),
                                chat.getAdmins()
                        );
                    })
                    .sorted((chat1, chat2) -> {
                        // First compare the last message timestamps (descending order)
                        int timestampComparison = Comparator.comparing(ChatList::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder()))
                                .compare(chat1, chat2);

                        // If timestamps are equal (both are null or identical), keep the order the same
                        return timestampComparison != 0 ? timestampComparison : 0;
                    })
                    .collect(Collectors.toList());


            return ResponseEntity.ok(new ResponseModel<>(true, "Success", chatLists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error fetching chat list: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public Integer calculateUnreadMessages(Long chatId, Long userId){
        List<Message> messageList = messageRepository.findByChatIdAndStatusIn(chatId, Arrays.asList(MessageStatus.SENT, MessageStatus.DELIVERED));
        Integer unreadMessageCount = (int) messageList.stream()
                .filter(message -> !Objects.equals(message.getSender().getId(), userId))
                .count();
        List<Message> updatedMessages = messageList.stream()
                .peek(message -> message.setStatus(MessageStatus.DELIVERED))
                .collect(Collectors.toList());
        messageRepository.saveAll(updatedMessages);
        return  unreadMessageCount;
    }


    public ResponseEntity<ResponseModel<?>> createChat(ChatDto chatDto) {
        try{
            Set<User> participants = new HashSet<>();
            Set<User> admins = new HashSet<>();
            for(User user : chatDto.getParticipants()){
                User existingUser = userRepository.findById(user.getId())
                        .orElseThrow(() -> new RuntimeException("Sender not found"));
                participants.add(existingUser);
            }
            for(User user : chatDto.getAdmins()){
                User existingUser = userRepository.findById(user.getId())
                        .orElseThrow(() -> new RuntimeException("Sender not found"));
                admins.add(existingUser);
            }

            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            modelMapper.getConfiguration().setPropertyCondition(conditions -> {
                return conditions.getSource() != null;
            });
            Chat chat = modelMapper.map(chatDto, Chat.class);
            chat.setAdmins(admins);
            chat.setParticipants(participants);
            chat.setCreatedAt(Instant.now());
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", chatRepository.save(chat)));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error creating Chat: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public ResponseEntity<ResponseModel<?>> getChatById(Long id) {
        try{
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", chatRepository.findById(id)));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public ResponseEntity<ResponseModel<?>> deleteUser(Long userId){
        try {
            if (!chatRepository.existsById(userId)) {
                // Return 404 Not Found
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseModel<>(false, "Chat not found"));
            }
            chatRepository.deleteById(userId);
            // Return 200 OK if the category is deleted successfully
            return ResponseEntity.ok(new ResponseModel<>(true, "Deleted successfully"));
        } catch (Exception e) {
            // Return 500 Internal Server Error for any unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error deleting chat: " + e.getMessage()));
        }
    }

    public ResponseEntity<ResponseModel<?>> getAllChats() {
        try{
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", chatRepository.findAll()));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error retrieving user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

    public void createChatsForNewUser(User newUser) {

        List<User> existingUsers = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(newUser.getId()))
                .toList();

        existingUsers.forEach(user -> {
            Optional<Chat> existingChat = chatRepository.findByParticipants(user.getId(), newUser.getId());

            if (existingChat.isEmpty()) {
                Chat chat = new Chat();

                // Store only the other person's name (like WhatsApp)
//                chat.setChatName(user.getUsername()); // user1 will see user2's name, and vice versa
                chat.setIsGroup(false);
                chat.setCreatedAt(Instant.now());
                chat.setParticipants(Set.of(user, newUser));
                chat.setCreatedAt(Instant.now());
                chatRepository.save(chat);

            }
        });
    }

    public void createChatsForUsers(List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            for (int j = i + 1; j < users.size(); j++) {
                User user1 = users.get(i);
                User user2 = users.get(j);

                // Check if a chat already exists between these two users
                Optional<Chat> existingChat = chatRepository.findByParticipants(user1.getId(), user2.getId());

                if (existingChat.isEmpty()) {
                    Chat chat = new Chat();
                    chat.setIsGroup(false);
                    chat.setCreatedAt(Instant.now());
                    chat.setParticipants(Set.of(user1, user2)); // Add both users to the chat

                    chatRepository.save(chat);
                }
            }
        }
    }

    public ResponseEntity<ResponseModel<?>> updateChat(ChatDto chatDto, Long chatId){
//        Optional<Chat> existingChat = chatRepository.findById(chatId);
//        if(existingChat.isPresent()){
//            Chat chat =existingChat.get();
//            chat.setParticipants(chatDto.getParticipants());
//            chat.setChatName(chatDto.getChatName());
//            chat.setAdmins(chatDto.getAdmins());
//
//
//        }

        try{
            Chat existingChatChat= chatRepository.findById(chatId).orElse(null);
            if (existingChatChat == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseModel<>(false, "Chat is not found with id: "+chatId));
            }

            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            modelMapper.getConfiguration().setPropertyCondition(conditions -> {
                return conditions.getSource() != null;
            });
            modelMapper.map(chatDto, existingChatChat);
            return ResponseEntity.ok(new ResponseModel<>(true, "Success", chatRepository.save(existingChatChat)));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseModel<>(false, "Error updating user details: " + exceptionHandlerUtil.sanitizeErrorMessage(e.getMessage())));
        }
    }

}







