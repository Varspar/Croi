package com.croi.voice.controller;

import com.croi.appointment.entity.CallRecording;
import com.croi.common.exception.UnauthorizedException;
import com.croi.common.exception.ErrorCode;
import com.croi.common.util.RateLimiter;
import com.croi.voice.dto.UpdateCallRecordingRequest;
import com.croi.voice.dto.VoiceCallRequest;
import com.croi.voice.dto.VoiceCallResponse;
import com.croi.voice.service.CallRecordingService;
import com.croi.voice.service.VoiceCallService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

/**
 * Webhook endpoints called by Asterisk on inbound calls — no user session exists
 * here, so there's no JWT (see SecurityConfig: these two paths are public and
 * gated by X-Api-Key instead).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/voice")
public class VoiceCallController {

    private static final int MAX_CALLS_PER_MINUTE = 100;

    private final VoiceCallService voiceCallService;
    private final CallRecordingService callRecordingService;
    private final RateLimiter rateLimiter;
    private final String webhookApiKey;

    public VoiceCallController(VoiceCallService voiceCallService, CallRecordingService callRecordingService,
                                RateLimiter rateLimiter, @Value("${voice.webhook-api-key}") String webhookApiKey) {
        this.voiceCallService = voiceCallService;
        this.callRecordingService = callRecordingService;
        this.rateLimiter = rateLimiter;
        this.webhookApiKey = webhookApiKey;
    }

    @PostMapping("/call-start")
    public ResponseEntity<VoiceCallResponse> callStart(@Valid @RequestBody VoiceCallRequest request,
                                                         HttpServletRequest servletRequest) {
        requireApiKey(servletRequest);
        // Keyed per-agent (available directly from the request body) rather than a strict
        // per-workspace aggregate — avoids a DB round-trip before the request is even
        // validated, and still caps the actual abuse vector (one runaway agent/integration).
        if (!rateLimiter.allowRequest("voice:agent:" + request.getAgentId(), MAX_CALLS_PER_MINUTE, Duration.ofMinutes(1))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many calls for this agent — please try again shortly");
        }

        log.info("Voice call-start: agent={} caller={}", request.getAgentId(), request.getCallerId());
        VoiceCallResponse response = voiceCallService.handleCall(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/call-end")
    public ResponseEntity<CallRecording> callEnd(@Valid @RequestBody UpdateCallRecordingRequest request,
                                                  HttpServletRequest servletRequest) {
        requireApiKey(servletRequest);

        log.info("Voice call-end: callId={} outcome={}", request.getCallId(), request.getOutcome());
        CallRecording recording = callRecordingService.endCall(
                request.getCallId(), request.getOutcome(), request.getDuration(), request.getNotes());
        return ResponseEntity.ok(recording);
    }

    private void requireApiKey(HttpServletRequest servletRequest) {
        if (webhookApiKey == null || webhookApiKey.isBlank()) {
            // Fails closed, same policy as RecaptchaService: an unset secret must never mean
            // "unauthenticated access is fine," it must mean "this webhook is off."
            log.error("VOICE_WEBHOOK_API_KEY is not set; rejecting all voice webhook requests");
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "Voice webhook is not configured");
        }
        String provided = servletRequest.getHeader("X-Api-Key");
        if (provided == null || !webhookApiKey.equals(provided)) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "Invalid or missing X-Api-Key");
        }
    }
}
