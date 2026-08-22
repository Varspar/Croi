package com.croi.knowledge.service;

import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ResourceNotFoundException;
import com.croi.conversation.entity.Conversation;
import com.croi.conversation.repository.ConversationRepository;
import com.croi.knowledge.dto.RagContext;
import com.croi.knowledge.dto.RagSource;
import com.croi.organization.entity.Organization;
import com.croi.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RagService {
    private final ConversationRepository conversationRepository;
    private final OrganizationRepository organizationRepository;
    private final OllamaEmbeddingService embeddingService;
    private final JdbcTemplate jdbcTemplate;

    public RagContext contextFor(UUID conversationId, String query) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CONVERSATION_NOT_FOUND, "Conversation not found"));
        Organization organization = organizationRepository.findById(conversation.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZATION_NOT_FOUND, "Organization not found"));
        List<RagSource> sources = search(organization, query);
        String prompt = renderPrompt(organization, sources);
        return new RagContext(prompt, sources, organization.getTemperature(), organization.getMaxTokens());
    }

    private List<RagSource> search(Organization organization, String query) {
        String vector = DocumentIngestionService.vectorLiteral(embeddingService.embed(query));
        return jdbcTemplate.query("SELECT d.id AS document_id, de.id AS chunk_id, d.file_name, de.content, "
                        + "(1 - (de.embedding <=> CAST(? AS vector))) AS score "
                        + "FROM document_embeddings de JOIN documents d ON d.id = de.document_id "
                        + "WHERE d.organization_id = ? AND d.status = 'READY' "
                        + "AND (1 - (de.embedding <=> CAST(? AS vector))) >= ? "
                        + "ORDER BY de.embedding <=> CAST(? AS vector) LIMIT ?",
                (rs, rowNum) -> new RagSource(UUID.fromString(rs.getString("document_id")),
                        UUID.fromString(rs.getString("chunk_id")), rs.getString("file_name"),
                        rs.getString("content"), rs.getDouble("score")),
                vector, organization.getId(), vector, organization.getSimilarityThreshold(), vector,
                organization.getMaxChunksInPrompt());
    }

    private String renderPrompt(Organization organization, List<RagSource> sources) {
        String base = organization.getSystemPrompt()
                .replace("{workspace_name}", organization.getName())
                .replace("{document_count}", Integer.toString(sources.stream().map(RagSource::documentId).collect(java.util.stream.Collectors.toSet()).size()))
                .replace("{chunk_count}", Integer.toString(sources.size()));
        String tone = "\nUse a " + organization.getTone().toLowerCase() + " tone.";
        if (sources.isEmpty()) return base + tone + "\nNo matching internal documents were found. Do not invent document-backed facts.";
        StringBuilder context = new StringBuilder(base).append(tone).append("\n\nRelevant internal documents:\n");
        for (int i = 0; i < sources.size(); i++) {
            RagSource source = sources.get(i);
            context.append("[Source ").append(i + 1).append(": ").append(source.documentName()).append("]\n")
                    .append(source.content()).append("\n\n");
        }
        return context.append("Answer using this context when relevant. If the context does not answer the question, say so plainly.").toString();
    }
}
