package com.croi.analytics.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * Basic workspace usage stats. Response-time metrics are deliberately omitted —
 * nothing in the request pipeline records timing yet, so a number here would be
 * fabricated rather than measured.
 */
@Builder
public record WorkspaceAnalyticsDto(
        long totalConversations,
        long totalMessages,
        /** Share of AI replies that cited at least one document chunk. Null if the workspace has sent no AI replies yet. */
        Double ragHitRate,
        /** Average wall-clock time to generate an AI reply. Null if no timed replies exist yet. */
        Double avgResponseTimeMs,
        List<DocumentCitation> topDocuments) {

    @Builder
    public record DocumentCitation(UUID documentId, String title, long citations) {
    }
}
