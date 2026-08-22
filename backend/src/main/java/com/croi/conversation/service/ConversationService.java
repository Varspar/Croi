package com.croi.conversation.service;

import com.croi.common.constants.ErrorMessages;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ResourceNotFoundException;
import com.croi.conversation.dto.ConversationDto;
import com.croi.conversation.dto.CreateConversationRequest;
import com.croi.conversation.entity.Conversation;
import com.croi.conversation.repository.ConversationRepository;
import com.croi.organization.repository.OrganizationMemberRepository;
import com.croi.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepository organizationRepository;

    public ConversationDto createConversation(CreateConversationRequest request, UUID userId) {
        assertMembership(request.getOrganizationId(), userId);
        Conversation conversation = Conversation.builder()
                .organizationId(request.getOrganizationId())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .build();

        return toDto(conversationRepository.save(conversation));
    }

    /**
     * Starts a conversation for the public guest chat widget — no user account, so no
     * membership check; the organization only needs to exist. The returned conversation
     * id is the guest's capability to keep talking (see GuestChatController).
     */
    public ConversationDto createGuestConversation(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException(ErrorCode.ORGANIZATION_NOT_FOUND, ErrorMessages.ORGANIZATION_NOT_FOUND);
        }
        Conversation conversation = Conversation.builder()
                .organizationId(organizationId)
                .build();
        return toDto(conversationRepository.save(conversation));
    }

    @Transactional(readOnly = true)
    public ConversationDto getConversation(UUID id, UUID userId) {
        Conversation conversation = findConversationOrThrow(id);
        assertMembership(conversation.getOrganizationId(), userId);
        return toDto(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> getConversationsByOrganization(UUID organizationId, UUID userId) {
        assertMembership(organizationId, userId);
        return conversationRepository.findByOrganizationId(organizationId).stream()
                .map(this::toDto)
                .toList();
    }

    public ConversationDto updateConversationStatus(UUID id, String status, UUID userId) {
        Conversation conversation = findConversationOrThrow(id);
        assertMembership(conversation.getOrganizationId(), userId);
        conversation.setStatus(status);
        return toDto(conversationRepository.save(conversation));
    }

    public ConversationDto escalateConversation(UUID id, UUID userId) {
        Conversation conversation = findConversationOrThrow(id);
        assertMembership(conversation.getOrganizationId(), userId);
        conversation.setIsEscalated(true);
        conversation.setStatus("ESCALATED");
        return toDto(conversationRepository.save(conversation));
    }

    private Conversation findConversationOrThrow(UUID id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CONVERSATION_NOT_FOUND, ErrorMessages.CONVERSATION_NOT_FOUND));
    }

    private void assertMembership(UUID organizationId, UUID userId) {
        if (!organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new com.croi.common.exception.UnauthorizedException(ErrorCode.WORKSPACE_MEMBER_ONLY, ErrorMessages.UNAUTHORIZED);
        }
    }

    private ConversationDto toDto(Conversation conversation) {
        return ConversationDto.builder()
                .id(conversation.getId())
                .organizationId(conversation.getOrganizationId())
                .customerName(conversation.getCustomerName())
                .customerEmail(conversation.getCustomerEmail())
                .status(conversation.getStatus())
                .isEscalated(conversation.getIsEscalated())
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
