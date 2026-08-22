package com.croi.conversation.controller;

import com.croi.common.dto.ApiResponse;
import com.croi.conversation.dto.MessageDto;
import com.croi.conversation.dto.SendMessageRequest;
import com.croi.conversation.service.MessageService;
import com.croi.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @Valid @RequestBody SendMessageRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.createMessage(request, principal.getId())));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessages(
            @PathVariable UUID conversationId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.getMessagesByConversation(conversationId, principal.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        messageService.deleteMessage(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Message deleted"));
    }
}
