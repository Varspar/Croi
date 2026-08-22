package com.croi.conversation.service;

import com.croi.ai.service.ChatService;
import com.croi.conversation.dto.MessageDto;
import com.croi.conversation.dto.SendMessageRequest;
import com.croi.conversation.entity.Conversation;
import com.croi.conversation.entity.Message;
import com.croi.conversation.repository.ConversationRepository;
import com.croi.conversation.repository.MessageRepository;
import com.croi.organization.repository.OrganizationMemberRepository;
import com.croi.knowledge.repository.MessageSourceRepository;
import com.croi.knowledge.repository.DocumentRepository;
import com.croi.knowledge.service.RagService;
import com.croi.knowledge.dto.RagContext;
import com.croi.ai.prompts.CustomerSupportPrompts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ChatService chatService;
    @Mock private ConversationRepository conversationRepository;
    @Mock private OrganizationMemberRepository organizationMemberRepository;
    @Mock private RagService ragService;
    @Mock private MessageSourceRepository messageSourceRepository;
    @Mock private DocumentRepository documentRepository;
    @InjectMocks private MessageService messageService;

    @Test
    void customerMessageIsPersistedAndTriggersOneAiReply() {
        UUID conversationId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Conversation conversation = Conversation.builder().organizationId(organizationId).build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, userId)).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            return message;
        });
        when(messageSourceRepository.findByMessageId(any(UUID.class))).thenReturn(java.util.List.of());
        when(ragService.contextFor(eq(conversationId), any(String.class)))
                .thenReturn(new RagContext(CustomerSupportPrompts.SYSTEM_PROMPT, java.util.List.of(), 0.7, 500));
        when(chatService.generateResponse(eq(conversationId), any(String.class), any(RagContext.class)))
                .thenReturn(MessageDto.builder().id(UUID.randomUUID()).content("AI reply").build());

        MessageDto result = messageService.createMessage(
                new SendMessageRequest(conversationId, "How do I reset my password?", "AI"), userId);

        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(message.capture());
        verify(chatService).generateResponse(eq(conversationId), eq("How do I reset my password?"), any(RagContext.class));
        assertEquals("CUSTOMER", message.getValue().getSenderType());
        assertEquals("How do I reset my password?", result.getContent());
    }
}
