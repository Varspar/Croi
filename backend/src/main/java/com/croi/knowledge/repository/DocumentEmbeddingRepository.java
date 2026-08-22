package com.croi.knowledge.repository;

import com.croi.knowledge.entity.DocumentEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, UUID> {

    List<DocumentEmbedding> findByDocumentId(UUID documentId);
}
