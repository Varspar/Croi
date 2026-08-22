package com.croi.analytics.service;

import com.croi.analytics.dto.WorkspaceAnalyticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    public WorkspaceAnalyticsDto getWorkspaceAnalytics(UUID organizationId) {
        long totalConversations = count(
                "SELECT COUNT(*) FROM conversations WHERE organization_id = ?", organizationId);

        long totalMessages = count(
                "SELECT COUNT(*) FROM messages m JOIN conversations c ON c.id = m.conversation_id "
                        + "WHERE c.organization_id = ?", organizationId);

        long aiMessages = count(
                "SELECT COUNT(*) FROM messages m JOIN conversations c ON c.id = m.conversation_id "
                        + "WHERE c.organization_id = ? AND m.sender_type = 'AI'", organizationId);

        long aiMessagesWithSources = count(
                "SELECT COUNT(DISTINCT ms.message_id) FROM message_sources ms "
                        + "JOIN messages m ON m.id = ms.message_id "
                        + "JOIN conversations c ON c.id = m.conversation_id "
                        + "WHERE c.organization_id = ?", organizationId);

        Double ragHitRate = aiMessages == 0 ? null : (double) aiMessagesWithSources / aiMessages;

        Double avgResponseTimeMs = jdbcTemplate.queryForObject(
                "SELECT AVG(m.response_time_ms) FROM messages m JOIN conversations c ON c.id = m.conversation_id "
                        + "WHERE c.organization_id = ? AND m.sender_type = 'AI' AND m.response_time_ms IS NOT NULL",
                Double.class, organizationId);

        List<WorkspaceAnalyticsDto.DocumentCitation> topDocuments = jdbcTemplate.query(
                "SELECT d.id AS document_id, d.title, COUNT(ms.id) AS citations "
                        + "FROM message_sources ms JOIN documents d ON d.id = ms.document_id "
                        + "WHERE d.organization_id = ? "
                        + "GROUP BY d.id, d.title ORDER BY citations DESC LIMIT 5",
                (rs, rowNum) -> new WorkspaceAnalyticsDto.DocumentCitation(
                        UUID.fromString(rs.getString("document_id")),
                        rs.getString("title"),
                        rs.getLong("citations")),
                organizationId);

        return WorkspaceAnalyticsDto.builder()
                .totalConversations(totalConversations)
                .totalMessages(totalMessages)
                .ragHitRate(ragHitRate)
                .avgResponseTimeMs(avgResponseTimeMs)
                .topDocuments(topDocuments)
                .build();
    }

    private long count(String sql, UUID organizationId) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class, organizationId);
        return result == null ? 0 : result;
    }
}
