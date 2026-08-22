package com.croi.common.exception;

import org.springframework.http.HttpStatus;

/** An upstream dependency (Ollama, OpenRouter, etc.) is unreachable or failing. */
public class ServiceUnavailableException extends ApiException {

    public ServiceUnavailableException(ErrorCode errorCode, String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, errorCode.name(), message);
    }
}
