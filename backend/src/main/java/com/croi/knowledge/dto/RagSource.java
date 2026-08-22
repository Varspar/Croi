package com.croi.knowledge.dto;

import java.util.UUID;

public record RagSource(UUID documentId, UUID chunkId, String documentName, String content, double relevanceScore) {
}
