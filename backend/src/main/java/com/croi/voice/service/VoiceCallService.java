package com.croi.voice.service;

import com.croi.ai.providers.openrouter.dto.OpenRouterRequest;
import com.croi.ai.providers.openrouter.dto.OpenRouterResponse;
import com.croi.appointment.entity.CallRecording;
import com.croi.common.constants.ErrorMessages;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ResourceNotFoundException;
import com.croi.voice.dto.VoiceCallRequest;
import com.croi.voice.dto.VoiceCallResponse;
import com.croi.voiceagent.entity.VoiceAgent;
import com.croi.voiceagent.entity.VoiceAgentConfig;
import com.croi.voiceagent.repository.VoiceAgentConfigRepository;
import com.croi.voiceagent.repository.VoiceAgentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Orchestrates one call turn: transcribe → reason → speak → record. Every step
 * that talks to an external service (Whisper, OpenRouter, Piper) degrades to a
 * safe fallback instead of throwing — a caller with a flaky STT/TTS/LLM backend
 * should still get *some* response, not a dropped call.
 */
@Slf4j
@Service
public class VoiceCallService {

    private static final String NO_SPEECH_DETECTED_MESSAGE = "Sorry, I didn't catch that — could you say that again?";
    private static final String LLM_UNAVAILABLE_MESSAGE = "Please hold while we connect you.";
    private static final int LLM_MAX_TOKENS = 300;

    private final VoiceAgentRepository voiceAgentRepository;
    private final VoiceAgentConfigRepository voiceAgentConfigRepository;
    private final WhisperService whisperService;
    private final PiperTtsService piperTtsService;
    private final CallRecordingService callRecordingService;
    private final RestTemplate restTemplate;
    private final String openRouterApiKey;
    private final String openRouterApiUrl;

    public VoiceCallService(VoiceAgentRepository voiceAgentRepository,
                             VoiceAgentConfigRepository voiceAgentConfigRepository,
                             WhisperService whisperService,
                             PiperTtsService piperTtsService,
                             CallRecordingService callRecordingService,
                             RestTemplateBuilder restTemplateBuilder,
                             @Value("${openrouter.api-key}") String openRouterApiKey,
                             @Value("${openrouter.api-url}") String openRouterApiUrl) {
        this.voiceAgentRepository = voiceAgentRepository;
        this.voiceAgentConfigRepository = voiceAgentConfigRepository;
        this.whisperService = whisperService;
        this.piperTtsService = piperTtsService;
        this.callRecordingService = callRecordingService;
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(10)).setReadTimeout(Duration.ofSeconds(15)).build();
        this.openRouterApiKey = openRouterApiKey;
        this.openRouterApiUrl = openRouterApiUrl;
    }

    public VoiceCallResponse handleCall(VoiceCallRequest request) {
        Instant now = Instant.now();

        VoiceAgent agent = voiceAgentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VOICE_AGENT_NOT_FOUND, ErrorMessages.VOICE_AGENT_NOT_FOUND));
        VoiceAgentConfig config = voiceAgentConfigRepository.findByAgentId(agent.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VOICE_AGENT_NOT_FOUND, ErrorMessages.VOICE_AGENT_NOT_FOUND));

        String transcript = whisperService.transcribeAudio(request.getAudioBase64());
        String agentReply = transcript.isBlank() ? NO_SPEECH_DETECTED_MESSAGE : generateReply(config, transcript);
        String replyAudio = piperTtsService.textToSpeech(agentReply);

        CallRecording recording = callRecordingService.createCallRecording(
                agent.getOrganizationId(), agent.getId(), request.getCallerId(), request.getTimestamp(), transcript, agentReply);
        callRecordingService.persistAudioFileAsync(recording.getId(), agent.getId(), request.getAudioBase64());

        return VoiceCallResponse.builder()
                .success(true)
                .callId(recording.getId())
                .agentResponse(agentReply)
                .audioBase64(replyAudio)
                .transcript(transcript)
                .timestamp(now)
                .build();
    }

    /** Never throws — an OpenRouter outage returns {@link #LLM_UNAVAILABLE_MESSAGE} instead. */
    private String generateReply(VoiceAgentConfig config, String callerTranscript) {
        if (openRouterApiKey == null || openRouterApiKey.isBlank()) {
            log.error("OPENROUTER_API_KEY is not set; cannot generate a voice agent reply");
            return LLM_UNAVAILABLE_MESSAGE;
        }

        List<OpenRouterRequest.Message> messages = List.of(
                new OpenRouterRequest.Message("system", config.getSystemPrompt()),
                new OpenRouterRequest.Message("user", callerTranscript));
        OpenRouterRequest llmRequest = new OpenRouterRequest(config.getModel(), messages, LLM_MAX_TOKENS, config.getTemperature());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openRouterApiKey);

        try {
            OpenRouterResponse response = restTemplate.postForObject(
                    openRouterApiUrl, new HttpEntity<>(llmRequest, headers), OpenRouterResponse.class);
            String text = extractText(response);
            return text == null || text.isBlank() ? LLM_UNAVAILABLE_MESSAGE : text;
        } catch (RestClientException ex) {
            log.error("OpenRouter call failed while generating a voice agent reply", ex);
            return LLM_UNAVAILABLE_MESSAGE;
        }
    }

    private String extractText(OpenRouterResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return null;
        }
        OpenRouterResponse.Message message = response.choices().get(0).message();
        return message == null ? null : message.content();
    }
}
