package com.pinebook.api.dto;

/**
 * Response item for GET /api/recommendations
 */
public record RecommendationDto(
        String id,
        String title,
        String topic,
        String level,
        int video_count
) {}
