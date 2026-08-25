package com.croi.common.exception;

/**
 * Machine-readable failure codes returned as {@code ApiError.errorCode}, so API
 * consumers (the frontend, tests, other clients) can branch on the kind of
 * failure instead of pattern-matching the human-readable message string.
 *
 * Deliberately scoped to the codes actual throw-sites use today rather than every
 * failure mode that could theoretically exist — an unused code is worse than no
 * code, since nothing verifies it stays accurate.
 */
public enum ErrorCode {
    // Generic fallbacks — used where no more specific code applies.
    RESOURCE_NOT_FOUND,
    VALIDATION_ERROR,
    UNAUTHORIZED,
    CONFLICT,
    ACCESS_DENIED,
    RATE_LIMIT_EXCEEDED,
    INTERNAL_ERROR,
    FILE_TOO_LARGE,

    // Auth
    INVALID_CREDENTIALS,
    INVALID_TOKEN,
    EMAIL_ALREADY_EXISTS,

    // Resources
    USER_NOT_FOUND,
    ORGANIZATION_NOT_FOUND,
    DOCUMENT_NOT_FOUND,
    APPOINTMENT_NOT_FOUND,
    APPOINTMENT_SLOT_UNAVAILABLE,
    VOICE_AGENT_NOT_FOUND,
    PHONE_NUMBER_ALREADY_BOUND,
    CALL_RECORDING_NOT_FOUND,
    SLUG_ALREADY_EXISTS,
    MEMBER_ALREADY_EXISTS,

    // Workspace access
    WORKSPACE_OWNER_ONLY,
    WORKSPACE_MEMBER_ONLY,

    // Documents / ingestion
    FILE_NOT_PDF,
    DOCUMENT_PROCESSING_FAILED,

    // RAG / embeddings
    OLLAMA_UNAVAILABLE,
}
