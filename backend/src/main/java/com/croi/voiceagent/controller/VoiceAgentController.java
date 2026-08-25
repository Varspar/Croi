package com.croi.voiceagent.controller;

import com.croi.common.constants.ErrorMessages;
import com.croi.common.dto.ApiResponse;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.UnauthorizedException;
import com.croi.organization.repository.OrganizationMemberRepository;
import com.croi.security.UserPrincipal;
import com.croi.voiceagent.dto.CreateVoiceAgentRequest;
import com.croi.voiceagent.dto.UpdateVoiceAgentConfigRequest;
import com.croi.voiceagent.dto.UpdateVoiceAgentRequest;
import com.croi.voiceagent.dto.VoiceAgentDto;
import com.croi.voiceagent.service.VoiceAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Config-only for now — no telephony/voice pipeline wired up yet. Every endpoint
 * requires an authenticated org member (JWT via JwtFilter), same as the rest of
 * the app.
 */
@RestController
@RequestMapping("/api/v1/voice-agents")
@RequiredArgsConstructor
public class VoiceAgentController {

    private final VoiceAgentService voiceAgentService;
    private final OrganizationMemberRepository membershipRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<VoiceAgentDto>> createAgent(
            @Valid @RequestBody CreateVoiceAgentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(request.getOrganizationId(), principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(voiceAgentService.createAgent(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoiceAgentDto>>> listAgents(
            @RequestParam UUID organizationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(voiceAgentService.getAgentsForOrganization(organizationId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoiceAgentDto>> getAgent(
            @PathVariable UUID id,
            @RequestParam UUID organizationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(voiceAgentService.getAgent(id, organizationId)));
    }

    /** Updates the agent itself (name, status, phone number). For system prompt/tone/model, see /config. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoiceAgentDto>> updateAgent(
            @PathVariable UUID id,
            @RequestParam UUID organizationId,
            @RequestBody UpdateVoiceAgentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(voiceAgentService.updateAgent(id, organizationId, request)));
    }

    /** Updates the agent's voice-pipeline config (system prompt, tone, temperature, model, max call duration). */
    @PostMapping("/{id}/config")
    public ResponseEntity<ApiResponse<VoiceAgentDto>> updateAgentConfig(
            @PathVariable UUID id,
            @RequestParam UUID organizationId,
            @Valid @RequestBody UpdateVoiceAgentConfigRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        requireMember(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(voiceAgentService.updateAgentConfig(id, organizationId, request)));
    }

    private void requireMember(UUID organizationId, UUID userId) {
        if (!membershipRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new UnauthorizedException(ErrorCode.WORKSPACE_MEMBER_ONLY, ErrorMessages.UNAUTHORIZED);
        }
    }
}
