package com.croi.ai.service;

import com.croi.ai.prompts.CustomerSupportPrompts;
import com.croi.ai.providers.openrouter.dto.OpenRouterRequest;
import com.croi.ai.providers.openrouter.dto.OpenRouterResponse;
import com.croi.conversation.dto.MessageDto;
import com.croi.conversation.entity.Message;
import com.croi.conversation.repository.MessageRepository;
import com.croi.knowledge.dto.RagContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChatService {

    private static final int DEFAULT_MAX_TOKENS = 500;
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final int SUMMARY_MAX_TOKENS = 200;
    private static final double SUMMARY_TEMPERATURE = 0.3;

    private static final String RATE_LIMIT_MESSAGE = "Please try again in a moment.";
    private static final String UNAVAILABLE_MESSAGE = "Service temporarily unavailable.";
    private static final String FALLBACK_MESSAGE =
            "Sorry, I'm having trouble responding right now. Please try again in a moment, "
                    + "or contact support if this continues.";
    private static final String SUMMARY_UNAVAILABLE = "(earlier messages could not be summarized)";

    private final RestTemplate restTemplate;
    private final MessageRepository messageRepository;
    private final String apiKey;
    private final String model;
    private final String apiUrl;

    public ChatService(RestTemplateBuilder restTemplateBuilder,
                        MessageRepository messageRepository,
                        @Value("${openrouter.api-key}") String apiKey,
                        @Value("${openrouter.model}") String model,
                        @Value("${openrouter.api-url}") String apiUrl) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
        this.messageRepository = messageRepository;
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
    }

    /**
     * Calls OpenRouter for a reply to {@code userMessage}, saves it as an AI message on
     * the conversation, and returns it. This must never throw — the customer's message has
     * already been persisted by the caller, and an OpenRouter outage must not undo that. On
     * failure a short, honest fallback message is saved instead of a fake AI reply.
     */
    public MessageDto generateResponse(UUID conversationId, String userMessage) {
        return generateResponse(conversationId, userMessage,
                new RagContext(CustomerSupportPrompts.SYSTEM_PROMPT, List.of(), DEFAULT_TEMPERATURE, DEFAULT_MAX_TOKENS));
    }

    public MessageDto generateResponse(UUID conversationId, String userMessage, RagContext context) {
        return generateResponse(conversationId, userMessage, context, null);
    }

    /**
     * @param priorHistory pre-built chat history (e.g. from ConversationMemoryService, which
     *                      summarizes older turns for long conversations). Pass null to fall
     *                      back to querying the last 15 raw messages, as before.
     */
    public MessageDto generateResponse(UUID conversationId, String userMessage, RagContext context,
                                        List<OpenRouterRequest.Message> priorHistory) {
        long startedAt = System.currentTimeMillis();
        String aiContent = callOpenRouter(conversationId, userMessage, context, priorHistory);
        int elapsedMs = (int) (System.currentTimeMillis() - startedAt);
        log.info("Chat response generated for conversation {} in {}ms", conversationId, elapsedMs);

        Message aiMessage = Message.builder()
                .conversationId(conversationId)
                .senderType("AI")
                .content(aiContent)
                .responseTimeMs(elapsedMs)
                .build();

        return toDto(messageRepository.save(aiMessage));
    }

    /**
     * Summarizes a block of transcript text in a couple of sentences, for use as
     * compressed context once a conversation outgrows the raw-history window.
     * Never throws — returns a placeholder string on any failure so a summarization
     * hiccup degrades to "less context" rather than breaking the chat turn.
     */
    public String generateSummary(String transcript) {
        if (apiKey == null || apiKey.isBlank() || transcript == null || transcript.isBlank()) {
            return SUMMARY_UNAVAILABLE;
        }

        List<OpenRouterRequest.Message> messages = List.of(
                new OpenRouterRequest.Message("system",
                        "Summarize the following support conversation in 2-3 sentences, "
                                + "focusing on the key topics, decisions, and facts discussed. "
                                + "Be concise; this summary replaces the raw transcript as context for later turns."),
                new OpenRouterRequest.Message("user", transcript));
        OpenRouterRequest request = new OpenRouterRequest(model, messages, SUMMARY_MAX_TOKENS, SUMMARY_TEMPERATURE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            OpenRouterResponse response = restTemplate.postForObject(
                    apiUrl, new HttpEntity<>(request, headers), OpenRouterResponse.class);
            String text = extractText(response);
            return (text == null || text.isBlank()) ? SUMMARY_UNAVAILABLE : text;
        } catch (RestClientException ex) {
            log.warn("Conversation summarization call failed", ex);
            return SUMMARY_UNAVAILABLE;
        }
    }

    private String callOpenRouter(UUID conversationId, String userMessage, RagContext context,
                                   List<OpenRouterRequest.Message> priorHistory) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("OPENROUTER_API_KEY is not set; skipping call to OpenRouter");
            return UNAVAILABLE_MESSAGE;
        }

        List<OpenRouterRequest.Message> messages = new java.util.ArrayList<>();
        messages.add(new OpenRouterRequest.Message("system", context.systemPrompt()));
        if (priorHistory != null) {
            // Already includes the just-saved current user message (built from the DB row).
            messages.addAll(priorHistory);
        } else {
            List<Message> history = messageRepository.findTop15ByConversationIdOrderByCreatedAtDesc(conversationId);
            java.util.Collections.reverse(history);
            for (Message item : history) {
                messages.add(new OpenRouterRequest.Message("CUSTOMER".equalsIgnoreCase(item.getSenderType()) ? "user" : "assistant", item.getContent()));
            }
            // The just-saved user message is in history; this guard keeps the method correct if invoked directly.
            if (history.isEmpty() || !userMessage.equals(history.get(history.size() - 1).getContent())) {
                messages.add(new OpenRouterRequest.Message("user", userMessage));
            }
        }
        OpenRouterRequest request = new OpenRouterRequest(model, messages, context.maxTokens(), context.temperature());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            OpenRouterResponse response = restTemplate.postForObject(
                    apiUrl, new HttpEntity<>(request, headers), OpenRouterResponse.class);
            String text = extractText(response);
            return (text == null || text.isBlank()) ? FALLBACK_MESSAGE : text;
        } catch (HttpClientErrorException.TooManyRequests ex) {
            log.warn("OpenRouter rate limit hit: {}", ex.getMessage());
            return RATE_LIMIT_MESSAGE;
        } catch (HttpClientErrorException.Unauthorized ex) {
            log.error("OpenRouter rejected the API key: {}", ex.getResponseBodyAsString());
            return UNAVAILABLE_MESSAGE;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("OpenRouter API returned an error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return FALLBACK_MESSAGE;
        } catch (ResourceAccessException ex) {
            log.error("Network error calling OpenRouter API", ex);
            return FALLBACK_MESSAGE;
        } catch (RestClientException ex) {
            log.error("Failed to call or parse the OpenRouter response", ex);
            return FALLBACK_MESSAGE;
        }
    }

    private String extractText(OpenRouterResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return null;
        }
        OpenRouterResponse.Message message = response.choices().get(0).message();
        return message == null ? null : message.content();
    }

    private MessageDto toDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
