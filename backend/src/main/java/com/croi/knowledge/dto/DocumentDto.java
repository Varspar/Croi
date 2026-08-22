package com.croi.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {

    private UUID id;
    private UUID organizationId;
    private String title;
    private String fileName;
    private String fileType;
    private String status;
    private Long fileSize;
    private String errorMessage;
    private Integer chunkCount;
    private Integer embeddingCount;
    private Instant createdAt;
}
