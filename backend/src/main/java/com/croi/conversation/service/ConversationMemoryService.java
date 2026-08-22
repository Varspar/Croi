package com.croi.conversation.service;

import com.croi.ai.providers.openrouter.dto.OpenRouterRequest;
import com.croi.ai.service.ChatService;
import com.croi.conversation.entity.Message;
import com.croi.conversation.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builds the chat history passed to OpenRouter for a turn. Short conversations pass
 * through as-is; once a conversation grows past {@link #SUMMARIZE_THRESHOLD} messages,
 * everything older than the last {@link #RECENT_WINDOW} turns is compressed into a single
 * summary message instead of being silently dropped, so a 30-turn conversation doesn't
 * lose everything before turn 15.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMemoryService {

    static final int RECENT_WINDOW = 15;
    static final int SUMMARIZE_THRESHOLD = 20;

    private final MessageRepository messageRepository;
    private final ChatService chatService;

    /**
     * @return chat history for the conversation, oldest first, ready to append after the
     * system prompt. Includes the just-saved current-turn user message — callers should not
     * append it again.
     */
    public List<OpenRouterRequest.Message> buildHistory(UUID conversationId) {
        List<Message> all = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (all.size() <= SUMMARIZE_THRESHOLD) {
            return toChatMessages(all);
        }

        List<Message> older = all.subList(0, all.size() - RECENT_WINDOW);
        List<Message> recent = all.subList(all.size() - RECENT_WINDOW, all.size());

        List<OpenRouterRequest.Message> history = new ArrayList<>();
        history.add(new OpenRouterRequest.Message("system", "Summary of earlier conversation: " + summarize(older)));
        history.addAll(toChatMessages(recent));
        return history;
    }

    private String summarize(List<Message> messages) {
        return chatService.generateSummary(formatTranscript(messages));
    }

    private String formatTranscript(List<Message> messages) {
        return messages.stream()
                .map(m -> ("CUSTOMER".equalsIgnoreCase(m.getSenderType()) ? "Customer" : "Assistant") + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    private List<OpenRouterRequest.Message> toChatMessages(List<Message> messages) {
        return messages.stream()
                .map(m -> new OpenRouterRequest.Message(
                        "CUSTOMER".equalsIgnoreCase(m.getSenderType()) ? "user" : "assistant", m.getContent()))
                .toList();
    }
}
