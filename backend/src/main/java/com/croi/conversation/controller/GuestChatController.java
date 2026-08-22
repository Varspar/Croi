package com.croi.conversation.controller;

import com.croi.common.dto.ApiResponse;
import com.croi.conversation.dto.ConversationDto;
import com.croi.conversation.dto.CreateGuestConversationRequest;
import com.croi.conversation.dto.MessageDto;
import com.croi.conversation.dto.SendMessageRequest;
import com.croi.conversation.service.ConversationService;
import com.croi.conversation.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Unauthenticated chat endpoints for the public guest widget (see the branded
 * /chat page). A guest is identified only by the conversation id handed back
 * from POST /conversations — an unguessable UUID, not a user account. There is
 * no membership check here by design; SecurityConfig permits this whole path
 * without a JWT. Do not add anything here beyond conversation-scoped chat.
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class GuestChatController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationDto>> createConversation(
            @Valid @RequestBody CreateGuestConversationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(conversationService.createGuestConversation(request.getOrganizationId())));
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.createGuestMessage(request)));
    }

    @GetMapping("/messages/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessages(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.getMessagesByConversationGuest(conversationId)));
    }
}
