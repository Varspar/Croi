package com.croi.voiceagent.service;

import com.croi.common.constants.ErrorMessages;
import com.croi.common.exception.ConflictException;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ResourceNotFoundException;
import com.croi.common.exception.UnauthorizedException;
import com.croi.voiceagent.dto.CreateVoiceAgentRequest;
import com.croi.voiceagent.dto.UpdateVoiceAgentConfigRequest;
import com.croi.voiceagent.dto.UpdateVoiceAgentRequest;
import com.croi.voiceagent.dto.VoiceAgentConfigDto;
import com.croi.voiceagent.dto.VoiceAgentDto;
import com.croi.voiceagent.entity.VoiceAgent;
import com.croi.voiceagent.entity.VoiceAgentConfig;
import com.croi.voiceagent.entity.VoiceAgentStatus;
import com.croi.voiceagent.repository.VoiceAgentConfigRepository;
import com.croi.voiceagent.repository.VoiceAgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VoiceAgentService {

    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful voice receptionist.";
    private static final String DEFAULT_TONE = "PROFESSIONAL";
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final String DEFAULT_MODEL = "anthropic/claude-3-haiku";
    private static final int DEFAULT_MAX_DURATION_SECONDS = 300;

    private final VoiceAgentRepository voiceAgentRepository;
    private final VoiceAgentConfigRepository voiceAgentConfigRepository;

    public VoiceAgentDto createAgent(CreateVoiceAgentRequest request) {
        if (request.getPhoneNumber() != null && voiceAgentRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException(ErrorCode.PHONE_NUMBER_ALREADY_BOUND, "That phone number is already bound to another agent");
        }

        VoiceAgent agent = VoiceAgent.builder()
                .organizationId(request.getOrganizationId())
                .name(request.getName())
                .status(VoiceAgentStatus.INACTIVE.name())
                .phoneNumber(request.getPhoneNumber())
                .build();
        agent = voiceAgentRepository.save(agent);

        VoiceAgentConfig config = VoiceAgentConfig.builder()
                .agentId(agent.getId())
                .systemPrompt(DEFAULT_SYSTEM_PROMPT)
                .tone(DEFAULT_TONE)
                .temperature(DEFAULT_TEMPERATURE)
                .model(DEFAULT_MODEL)
                .maxDurationSeconds(DEFAULT_MAX_DURATION_SECONDS)
                .build();
        config = voiceAgentConfigRepository.save(config);

        return toDto(agent, config);
    }

    @Transactional(readOnly = true)
    public List<VoiceAgentDto> getAgentsForOrganization(UUID organizationId) {
        return voiceAgentRepository.findByOrganizationId(organizationId).stream()
                .map(agent -> toDto(agent, findConfigOrThrow(agent.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public VoiceAgentDto getAgent(UUID agentId, UUID organizationId) {
        VoiceAgent agent = findOwnedOrThrow(agentId, organizationId);
        return toDto(agent, findConfigOrThrow(agent.getId()));
    }

    public VoiceAgentDto updateAgent(UUID agentId, UUID organizationId, UpdateVoiceAgentRequest request) {
        VoiceAgent agent = findOwnedOrThrow(agentId, organizationId);

        if (request.getName() != null) agent.setName(request.getName());
        if (request.getStatus() != null) agent.setStatus(request.getStatus());
        if (request.getPhoneNumber() != null) agent.setPhoneNumber(request.getPhoneNumber());

        agent = voiceAgentRepository.save(agent);
        return toDto(agent, findConfigOrThrow(agent.getId()));
    }

    public VoiceAgentDto updateAgentConfig(UUID agentId, UUID organizationId, UpdateVoiceAgentConfigRequest request) {
        findOwnedOrThrow(agentId, organizationId);
        VoiceAgentConfig config = findConfigOrThrow(agentId);

        config.setSystemPrompt(request.getSystemPrompt());
        config.setTone(request.getTone());
        config.setTemperature(request.getTemperature());
        config.setModel(request.getModel());
        config.setMaxDurationSeconds(request.getMaxDurationSeconds());
        config = voiceAgentConfigRepository.save(config);

        VoiceAgent agent = voiceAgentRepository.findById(agentId).orElseThrow();
        return toDto(agent, config);
    }

    private VoiceAgent findOwnedOrThrow(UUID agentId, UUID organizationId) {
        VoiceAgent agent = voiceAgentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VOICE_AGENT_NOT_FOUND, ErrorMessages.VOICE_AGENT_NOT_FOUND));
        if (!agent.getOrganizationId().equals(organizationId)) {
            throw new UnauthorizedException(ErrorCode.WORKSPACE_MEMBER_ONLY, ErrorMessages.UNAUTHORIZED);
        }
        return agent;
    }

    private VoiceAgentConfig findConfigOrThrow(UUID agentId) {
        return voiceAgentConfigRepository.findByAgentId(agentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VOICE_AGENT_NOT_FOUND, ErrorMessages.VOICE_AGENT_NOT_FOUND));
    }

    private VoiceAgentDto toDto(VoiceAgent agent, VoiceAgentConfig config) {
        return VoiceAgentDto.builder()
                .id(agent.getId())
                .organizationId(agent.getOrganizationId())
                .name(agent.getName())
                .status(agent.getStatus())
                .phoneNumber(agent.getPhoneNumber())
                .config(VoiceAgentConfigDto.builder()
                        .systemPrompt(config.getSystemPrompt())
                        .tone(config.getTone())
                        .temperature(config.getTemperature())
                        .model(config.getModel())
                        .maxDurationSeconds(config.getMaxDurationSeconds())
                        .build())
                .createdAt(agent.getCreatedAt())
                .build();
    }
}
