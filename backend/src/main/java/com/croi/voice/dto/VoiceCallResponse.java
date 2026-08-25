package com.croi.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCallResponse {

    private boolean success;

    /** The id of the CallRecording this turn was saved under — pass it to /call-end. */
    private UUID callId;

    private String agentResponse;

    /** Base64-encoded WAV of {@link #agentResponse} spoken by Piper. Empty string if TTS failed. */
    private String audioBase64;

    /** What Whisper transcribed from the caller's audio. Empty string if STT failed. */
    private String transcript;

    private Instant timestamp;
}
