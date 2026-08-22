package com.croi.conversation.dto;

import lombok.Builder;
import java.util.UUID;

@Builder
public record MessageSourceDto(UUID documentId, UUID chunkId, String documentName, String content, Double relevanceScore) {
}
