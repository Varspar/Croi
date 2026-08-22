package com.croi.knowledge.repository;

import com.croi.knowledge.entity.MessageSource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MessageSourceRepository extends JpaRepository<MessageSource, UUID> {
    List<MessageSource> findByMessageId(UUID messageId);
}
