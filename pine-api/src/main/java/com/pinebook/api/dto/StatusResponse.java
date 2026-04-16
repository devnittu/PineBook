package com.pinebook.api.dto;

/**
 * Response body for GET /status/{videoId}
 */
public record StatusResponse(
        String videoId,
        String status
) {}
