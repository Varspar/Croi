package com.croi.organization.entity;

import com.croi.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Organization extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "system_prompt", columnDefinition = "TEXT", nullable = false)
    @Builder.Default
    private String systemPrompt = "You are Croi, {workspace_name}'s AI support assistant. Use the provided documents to answer questions.";

    @Column(nullable = false)
    @Builder.Default
    private String tone = "PROFESSIONAL";

    @Column(name = "embedding_model", nullable = false)
    @Builder.Default
    private String embeddingModel = "nomic-embed-text";

    @Column(name = "similarity_threshold", nullable = false)
    @Builder.Default
    private Double similarityThreshold = 0.3;

    @Column(name = "max_chunks_in_prompt", nullable = false)
    @Builder.Default
    private Integer maxChunksInPrompt = 5;

    @Column(nullable = false)
    @Builder.Default
    private Double temperature = 0.7;

    @Column(name = "max_tokens", nullable = false)
    @Builder.Default
    private Integer maxTokens = 500;

    /** Public branding for the guest chat widget — all optional, all nullable. */
    @Column(length = 500)
    private String logo;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(columnDefinition = "TEXT")
    private String description;
}
