package com.pinebook.api.dto;

/**
 * Response body for POST /api/chat
 */
public record ChatResponse(
        String reply
) {}
