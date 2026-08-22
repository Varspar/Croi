package com.croi.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceConfigurationDto {
    private String systemPrompt;
    private String tone;
    private String embeddingModel;
    private Double similarityThreshold;
    private Integer maxChunksInPrompt;
    private Double temperature;
    private Integer maxTokens;
}
