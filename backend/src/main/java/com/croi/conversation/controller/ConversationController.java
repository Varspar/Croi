package com.croi.conversation.controller;

import com.croi.common.dto.ApiResponse;
import com.croi.conversation.dto.ConversationDto;
import com.croi.conversation.dto.CreateConversationRequest;
import com.croi.conversation.dto.UpdateConversationStatusRequest;
import com.croi.conversation.service.ConversationService;
import com.croi.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationDto>> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(conversationService.createConversation(request, principal.getId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationDto>>> listConversations(
            @RequestParam UUID organizationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(conversationService.getConversationsByOrganization(organizationId, principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationDto>> getConversation(
            @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(conversationService.getConversation(id, principal.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationDto>> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateConversationStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(conversationService.updateConversationStatus(id, request.getStatus(), principal.getId())));
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<ApiResponse<ConversationDto>> escalate(
            @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(conversationService.escalateConversation(id, principal.getId())));
    }
}
