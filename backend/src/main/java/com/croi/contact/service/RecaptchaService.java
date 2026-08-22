package com.croi.contact.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class RecaptchaService {

    private static final double SCORE_THRESHOLD = 0.5;

    private final RestTemplate restTemplate;
    private final String secretKey;
    private final String verifyUrl;

    public RecaptchaService(RestTemplateBuilder restTemplateBuilder,
                             @Value("${recaptcha.secret-key}") String secretKey,
                             @Value("${recaptcha.verify-url}") String verifyUrl) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        this.secretKey = secretKey;
        this.verifyUrl = verifyUrl;
    }

    /**
     * Verifies a reCAPTCHA v3 token: must come back Google-verified, for the
     * expected action, with a bot-likelihood score at or above the threshold
     * (closer to 1.0 = more likely human). Fails CLOSED if the secret key isn't
     * configured — an unconfigured verifier rejects everything rather than
     * silently letting all submissions through unchecked.
     */
    public boolean verify(String token, String expectedAction) {
        if (secretKey == null || secretKey.isBlank()) {
            log.error("RECAPTCHA_SECRET_KEY is not set; rejecting submission (failing closed)");
            return false;
        }
        if (token == null || token.isBlank()) {
            return false;
        }

        // Google's siteverify endpoint expects a form-urlencoded POST body, not JSON.
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            RecaptchaVerifyResponse verifyResponse = restTemplate.postForObject(
                    verifyUrl, new HttpEntity<>(body, headers), RecaptchaVerifyResponse.class);

            if (verifyResponse == null || !verifyResponse.success()) {
                log.warn("reCAPTCHA verification failed: {}",
                        verifyResponse == null ? "no response" : verifyResponse.errorCodes());
                return false;
            }
            if (verifyResponse.score() != null && verifyResponse.score() < SCORE_THRESHOLD) {
                log.warn("reCAPTCHA score {} below threshold {}", verifyResponse.score(), SCORE_THRESHOLD);
                return false;
            }
            if (expectedAction != null && verifyResponse.action() != null
                    && !expectedAction.equals(verifyResponse.action())) {
                log.warn("reCAPTCHA action mismatch: expected '{}' but got '{}'",
                        expectedAction, verifyResponse.action());
                return false;
            }
            return true;
        } catch (RestClientException ex) {
            log.error("Failed to call reCAPTCHA verify endpoint", ex);
            return false;
        }
    }

    private record RecaptchaVerifyResponse(
            boolean success,
            Double score,
            String action,
            @JsonProperty("error-codes") List<String> errorCodes) {
    }
}
