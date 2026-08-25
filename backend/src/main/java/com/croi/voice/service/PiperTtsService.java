package com.croi.voice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/** Text-to-speech via a local Piper TTS server. */
@Slf4j
@Service
public class PiperTtsService {

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public PiperTtsService(RestTemplateBuilder builder, @Value("${piper.api-url}") String apiUrl) {
        this.restTemplate = builder.setConnectTimeout(Duration.ofSeconds(2)).setReadTimeout(Duration.ofSeconds(5)).build();
        this.apiUrl = apiUrl;
    }

    /**
     * @return base64-encoded WAV audio of {@code text} spoken aloud, or "" (silence) if
     * synthesis failed for any reason — never throws, so a Piper outage degrades the call
     * to text-only rather than breaking it.
     */
    public String textToSpeech(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("text", text), headers);
            byte[] wavBytes = restTemplate.postForObject(apiUrl, request, byte[].class);
            return wavBytes == null || wavBytes.length == 0 ? "" : Base64.getEncoder().encodeToString(wavBytes);
        } catch (ResourceAccessException ex) {
            log.error("Piper request timed out or the server is unreachable at {}", apiUrl, ex);
            return "";
        } catch (RestClientException ex) {
            log.error("Piper text-to-speech failed", ex);
            return "";
        }
    }
}
