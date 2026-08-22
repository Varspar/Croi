package com.croi.knowledge.service;

import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ServiceUnavailableException;
import com.croi.knowledge.dto.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OllamaEmbeddingService {
    public static final int DIMENSIONS = 768;
    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String model;

    public OllamaEmbeddingService(RestTemplateBuilder builder,
                                  @Value("${ollama.api-url:http://localhost:11434/api/embeddings}") String apiUrl,
                                  @Value("${ollama.embedding-model:nomic-embed-text}") String model) {
        this.restTemplate = builder.setConnectTimeout(Duration.ofSeconds(5)).setReadTimeout(Duration.ofSeconds(60)).build();
        this.apiUrl = apiUrl;
        this.model = model;
    }

    public float[] embed(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            EmbeddingResponse response = restTemplate.postForObject(apiUrl,
                    new HttpEntity<>(Map.of("model", model, "prompt", text), headers), EmbeddingResponse.class);
            List<Double> values = response == null ? null : response.embedding();
            if (values == null || values.size() != DIMENSIONS) {
                throw new ServiceUnavailableException(ErrorCode.OLLAMA_UNAVAILABLE, "Ollama returned an embedding with an unexpected dimension");
            }
            float[] embedding = new float[DIMENSIONS];
            for (int i = 0; i < DIMENSIONS; i++) embedding[i] = values.get(i).floatValue();
            return embedding;
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException(ErrorCode.OLLAMA_UNAVAILABLE, "Could not generate embedding. Ensure Ollama and nomic-embed-text are running.");
        }
    }
}
