package com.croi.voice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Base64;

/** Speech-to-text via a local Faster Whisper server (OpenAI-compatible /v1/audio/transcriptions). */
@Slf4j
@Service
public class WhisperService {

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public WhisperService(RestTemplateBuilder builder, @Value("${whisper.api-url}") String apiUrl) {
        // Short timeouts by design: this sits in the hot path of a live phone call.
        this.restTemplate = builder.setConnectTimeout(Duration.ofSeconds(2)).setReadTimeout(Duration.ofSeconds(5)).build();
        this.apiUrl = apiUrl;
    }

    /**
     * @return the transcript, or "" if transcription failed for any reason — this must
     * never throw, since a caller with no STT service running should still get a (degraded)
     * response rather than a 500.
     */
    public String transcribeAudio(String audioBase64) {
        if (audioBase64 == null || audioBase64.isBlank()) {
            return "";
        }

        byte[] audioBytes;
        try {
            audioBytes = Base64.getDecoder().decode(audioBase64);
        } catch (IllegalArgumentException ex) {
            log.error("Whisper transcription skipped: audioBase64 was not valid base64", ex);
            return "";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return "audio.wav";
                }
            });

            WhisperResponse response = restTemplate.postForObject(apiUrl, new HttpEntity<>(body, headers), WhisperResponse.class);
            return response == null || response.text() == null ? "" : response.text();
        } catch (ResourceAccessException ex) {
            log.error("Whisper request timed out or the server is unreachable at {}", apiUrl, ex);
            return "";
        } catch (RestClientException ex) {
            log.error("Whisper transcription failed", ex);
            return "";
        }
    }

    private record WhisperResponse(String text) {
    }
}
