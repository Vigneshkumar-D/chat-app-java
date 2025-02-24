package com.chat_app.repository.chat;

import com.chat_app.entity.chat.Message;
import com.chat_app.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp DESC LIMIT 1")
    Optional<Message> findLastMessageByChatId(@Param("chatId") Long chatId);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp ASC")
    List<Message> findByChatId(@Param("chatId") Long chatId);

    @Query("SELECT m FROM Message m LEFT JOIN m.recipient r WHERE m.sender.id = :userId OR r.id = :userId ORDER BY m.timestamp ASC")
    List<Message> findBySenderIdOrRecipientId(@Param("userId") Long userId);


    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.timestamp ASC")
    List<Message> findByChatIdAndContentContainingIgnoreCase(@Param("chatId") Long chatId, @Param("keyword") String keyword);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp DESC")
    Optional<Message> findTopByChatIdOrderByTimestampDesc(@Param("chatId") Long chatId);

    List<Message> findByChatIdAndStatusIn(Long chatId, List<MessageStatus> statuses);

}