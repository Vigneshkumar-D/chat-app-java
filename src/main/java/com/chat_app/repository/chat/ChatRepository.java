package com.chat_app.repository.chat;

import com.chat_app.entity.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByParticipants_Id(Long userId);

    @Query("SELECT c FROM Chat c " +
            "JOIN c.participants u1 " +
            "JOIN c.participants u2 " +
            "WHERE u1.id = :user1 AND u2.id = :user2 AND c.isGroup = false")
    Optional<Chat> findByParticipants(@Param("user1") Long user1, @Param("user2") Long user2);

}