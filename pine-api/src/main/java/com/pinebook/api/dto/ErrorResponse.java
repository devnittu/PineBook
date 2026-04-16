package com.pinebook.api.dto;

/**
 * Shared error response envelope.
 */
public record ErrorResponse(
        String error,
        String code
) {}
