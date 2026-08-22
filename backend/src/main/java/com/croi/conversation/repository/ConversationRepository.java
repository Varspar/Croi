package com.croi.conversation.repository;

import com.croi.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByOrganizationId(UUID organizationId);

    List<Conversation> findByStatus(String status);
}
