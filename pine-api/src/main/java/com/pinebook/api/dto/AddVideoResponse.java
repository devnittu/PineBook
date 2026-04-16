package com.pinebook.api.dto;

/**
 * Response body for POST /add-video
 */
public record AddVideoResponse(
        String videoId,
        String status
) {}
