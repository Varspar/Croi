package com.croi.voice.service;

import com.croi.appointment.entity.CallRecording;
import com.croi.common.exception.ResourceNotFoundException;
import com.croi.voice.dto.VoiceCallRequest;
import com.croi.voice.dto.VoiceCallResponse;
import com.croi.voiceagent.entity.VoiceAgent;
import com.croi.voiceagent.entity.VoiceAgentConfig;
import com.croi.voiceagent.repository.VoiceAgentConfigRepository;
import com.croi.voiceagent.repository.VoiceAgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the call-handling logic itself (VoiceCallService), not the thin
 * controller — matches this codebase's existing test style (plain Mockito, no
 * MockMvc/@WebMvcTest precedent exists here) and is where the actual behavior lives.
 */
@ExtendWith(MockitoExtension.class)
class VoiceCallServiceTest {

    @Mock private VoiceAgentRepository voiceAgentRepository;
    @Mock private VoiceAgentConfigRepository voiceAgentConfigRepository;
    @Mock private WhisperService whisperService;
    @Mock private PiperTtsService piperTtsService;
    @Mock private CallRecordingService callRecordingService;

    private VoiceAgent agent;
    private VoiceAgentConfig config;

    @BeforeEach
    void setUp() {
        agent = VoiceAgent.builder().organizationId(UUID.randomUUID()).name("Front Desk").status("ACTIVE").build();
        agent.setId(UUID.randomUUID());
        config = VoiceAgentConfig.builder()
                .agentId(agent.getId()).systemPrompt("You are a receptionist.").tone("PROFESSIONAL")
                .temperature(0.7).model("anthropic/claude-3-haiku").maxDurationSeconds(300).build();
    }

    private VoiceCallService serviceWith(String openRouterApiKey) {
        // A real (unmocked) RestTemplateBuilder is safe here — it's only ever exercised
        // when openRouterApiKey is non-blank, which none of these tests do, so no real
        // network call ever happens.
        return new VoiceCallService(voiceAgentRepository, voiceAgentConfigRepository, whisperService,
                piperTtsService, callRecordingService, new RestTemplateBuilder(), openRouterApiKey, "http://openrouter.invalid");
    }

    private VoiceCallRequest request() {
        VoiceCallRequest request = new VoiceCallRequest();
        request.setAgentId(agent.getId());
        request.setCallerId("+353871234567");
        request.setAudioBase64("bW9jay1hdWRpbw==");
        return request;
    }

    @Test
    void handleCall_unknownAgentId_throwsResourceNotFound() {
        when(voiceAgentRepository.findById(any())).thenReturn(Optional.empty());
        VoiceCallService service = serviceWith("");

        assertThrows(ResourceNotFoundException.class, () -> service.handleCall(request()));
        verify(callRecordingService, never()).createCallRecording(any(), any(), any(), any(), any(), any());
    }

    @Test
    void handleCall_whisperReturnsNoTranscript_stillRespondsGracefullyWithoutCallingTheLlm() {
        when(voiceAgentRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(voiceAgentConfigRepository.findByAgentId(agent.getId())).thenReturn(Optional.of(config));
        when(whisperService.transcribeAudio(anyString())).thenReturn(""); // Whisper failure/no speech
        when(piperTtsService.textToSpeech(anyString())).thenReturn("cGlwZXItYXVkaW8=");
        UUID recordingId = UUID.randomUUID();
        CallRecording recording = CallRecording.builder().id(recordingId).build();
        when(callRecordingService.createCallRecording(any(), any(), any(), any(), any(), any())).thenReturn(recording);

        VoiceCallResponse response = serviceWith("").handleCall(request());

        assertTrue(response.isSuccess());
        assertEquals("", response.getTranscript());
        assertEquals("Sorry, I didn't catch that — could you say that again?", response.getAgentResponse());
        assertEquals(recordingId, response.getCallId());
    }

    @Test
    void handleCall_piperFails_returnsEmptyAudioInsteadOfThrowing() {
        when(voiceAgentRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(voiceAgentConfigRepository.findByAgentId(agent.getId())).thenReturn(Optional.of(config));
        when(whisperService.transcribeAudio(anyString())).thenReturn("What are your hours?");
        when(piperTtsService.textToSpeech(anyString())).thenReturn(""); // Piper failure → silence
        when(callRecordingService.createCallRecording(any(), any(), any(), any(), any(), any()))
                .thenReturn(CallRecording.builder().id(UUID.randomUUID()).build());

        // Blank OpenRouter key forces the LLM-unavailable fallback path with no real network call.
        VoiceCallResponse response = serviceWith("").handleCall(request());

        assertTrue(response.isSuccess());
        assertEquals("", response.getAudioBase64());
    }

    @Test
    void handleCall_savesACallRecordingWithTheResolvedOrganizationAndAgent() {
        when(voiceAgentRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(voiceAgentConfigRepository.findByAgentId(agent.getId())).thenReturn(Optional.of(config));
        when(whisperService.transcribeAudio(anyString())).thenReturn("What are your hours?");
        when(piperTtsService.textToSpeech(anyString())).thenReturn("cGlwZXItYXVkaW8=");
        UUID recordingId = UUID.randomUUID();
        when(callRecordingService.createCallRecording(any(), any(), any(), any(), any(), any()))
                .thenReturn(CallRecording.builder().id(recordingId).build());

        VoiceCallRequest request = request();
        serviceWith("").handleCall(request);

        verify(callRecordingService).createCallRecording(
                eq(agent.getOrganizationId()), eq(agent.getId()), eq(request.getCallerId()),
                eq(request.getTimestamp()), eq("What are your hours?"), anyString());
        verify(callRecordingService).persistAudioFileAsync(recordingId, agent.getId(), request.getAudioBase64());
    }
}
