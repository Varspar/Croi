package com.croi.knowledge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import com.croi.common.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "message_sources")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class MessageSource extends BaseEntity {
    @Column(name = "message_id", nullable = false) private UUID messageId;
    @Column(name = "document_id", nullable = false) private UUID documentId;
    @Column(name = "chunk_id", nullable = false) private UUID chunkId;
    @Column(name = "relevance_score", nullable = false) private Double relevanceScore;
}
