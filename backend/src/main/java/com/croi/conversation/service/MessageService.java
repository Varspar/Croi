package com.croi.conversation.service;

import com.croi.ai.providers.openrouter.dto.OpenRouterRequest;
import com.croi.ai.service.ChatService;
import com.croi.common.constants.ErrorMessages;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ResourceNotFoundException;
import com.croi.conversation.dto.MessageDto;
import com.croi.conversation.dto.MessageSourceDto;
import com.croi.conversation.dto.SendMessageRequest;
import com.croi.conversation.entity.Message;
import com.croi.conversation.repository.MessageRepository;
import com.croi.conversation.entity.Conversation;
import com.croi.conversation.repository.ConversationRepository;
import com.croi.organization.repository.OrganizationMemberRepository;
import com.croi.knowledge.dto.RagContext;
import com.croi.knowledge.dto.RagSource;
import com.croi.knowledge.entity.MessageSource;
import com.croi.knowledge.repository.MessageSourceRepository;
import com.croi.knowledge.repository.DocumentRepository;
import com.croi.knowledge.repository.DocumentEmbeddingRepository;
import com.croi.knowledge.service.RagService;
import com.croi.ai.prompts.CustomerSupportPrompts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final String CUSTOMER_SENDER_TYPE = "CUSTOMER";

    private final MessageRepository messageRepository;
    private final ChatService chatService;
    private final ConversationRepository conversationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final RagService ragService;
    private final MessageSourceRepository messageSourceRepository;
    private final DocumentRepository documentRepository;
    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final ConversationMemoryService conversationMemoryService;

    /**
     * Saves the message, then — for customer messages — triggers the AI reply.
     * Deliberately NOT @Transactional: messageRepository.save() already commits in its
     * own transaction, so a slow or failing OpenRouter call in generateResponse() never holds
     * open (or rolls back) the database transaction that persisted the customer's message.
     */
    public MessageDto createMessage(SendMessageRequest request, UUID userId) {
        assertConversationAccess(request.getConversationId(), userId);
        return doCreateMessage(request.getConversationId(), request.getContent());
    }

    /**
     * Same flow as {@link #createMessage}, for the unauthenticated guest chat widget.
     * There's no user to check membership for — knowledge of the conversation id (an
     * unguessable UUID, handed out once at guest-conversation creation) is the only
     * access control. See GuestChatController.
     */
    public MessageDto createGuestMessage(SendMessageRequest request) {
        findConversationOrThrow(request.getConversationId());
        return doCreateMessage(request.getConversationId(), request.getContent());
    }

    private MessageDto doCreateMessage(UUID conversationId, String content) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderType(CUSTOMER_SENDER_TYPE)
                .content(content)
                .build();
        MessageDto saved = toDto(messageRepository.save(message));

        try {
            RagContext context;
            try {
                context = ragService.contextFor(conversationId, content);
            } catch (Exception ragFailure) {
                log.warn("RAG retrieval unavailable for conversation {}; continuing without document context", conversationId, ragFailure);
                context = new RagContext(CustomerSupportPrompts.SYSTEM_PROMPT, List.of(), 0.7, 500);
            }
            List<OpenRouterRequest.Message> history = null;
            try {
                history = conversationMemoryService.buildHistory(conversationId);
            } catch (Exception memoryFailure) {
                log.warn("Conversation memory unavailable for conversation {}; falling back to raw recent history", conversationId, memoryFailure);
            }
            MessageDto aiMessage = chatService.generateResponse(conversationId, content, context, history);
            for (RagSource source : context.sources()) {
                messageSourceRepository.save(MessageSource.builder().messageId(aiMessage.getId())
                        .documentId(source.documentId()).chunkId(source.chunkId()).relevanceScore(source.relevanceScore()).build());
            }
        } catch (Exception ex) {
            log.error("AI response generation failed for conversation {}", conversationId, ex);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByConversation(UUID conversationId, UUID userId) {
        assertConversationAccess(conversationId, userId);
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesByConversationGuest(UUID conversationId) {
        findConversationOrThrow(conversationId);
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void deleteMessage(UUID id, UUID userId) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MESSAGE_NOT_FOUND, ErrorMessages.MESSAGE_NOT_FOUND));
        assertConversationAccess(message.getConversationId(), userId);
        messageRepository.delete(message);
    }

    private MessageDto toDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .sources(messageSourceRepository.findByMessageId(message.getId()).stream().map(source ->
                        MessageSourceDto.builder().documentId(source.getDocumentId()).chunkId(source.getChunkId())
                                .documentName(documentRepository.findById(source.getDocumentId()).map(d -> d.getFileName()).orElse("Document"))
                                .content(documentEmbeddingRepository.findById(source.getChunkId()).map(chunk -> chunk.getContent()).orElse(null))
                                .relevanceScore(source.getRelevanceScore()).build()).toList())
                .build();
    }

    private void assertConversationAccess(UUID conversationId, UUID userId) {
        Conversation conversation = findConversationOrThrow(conversationId);
        if (!organizationMemberRepository.existsByOrganizationIdAndUserId(conversation.getOrganizationId(), userId)) {
            throw new com.croi.common.exception.UnauthorizedException(ErrorCode.WORKSPACE_MEMBER_ONLY, ErrorMessages.UNAUTHORIZED);
        }
    }

    private Conversation findConversationOrThrow(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CONVERSATION_NOT_FOUND, ErrorMessages.CONVERSATION_NOT_FOUND));
    }
}
