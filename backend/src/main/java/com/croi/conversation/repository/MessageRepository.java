package com.croi.conversation.repository;

import com.croi.conversation.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationId(UUID conversationId);

    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<Message> findTop15ByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
