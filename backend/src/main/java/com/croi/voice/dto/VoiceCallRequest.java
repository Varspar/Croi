package com.croi.voice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * One request = one turn's audio in, one reply out — see VoiceCallService for
 * why this sprint doesn't correlate multiple turns into a single call session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCallRequest {

    @NotNull
    private UUID agentId;

    @NotBlank
    private String callerId;

    private String phoneNumber;

    @NotBlank
    private String audioBase64;

    private Instant timestamp;

    /** Seconds elapsed in the call so far, if the caller (Asterisk) tracks it. Optional. */
    private Integer callDuration;
}
