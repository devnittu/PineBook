package com.pinebook.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response body for GET /api/path/:id
 */
public record LearningPathDto(
        String id,
        String title,
        List<PhaseDto> phases,
        int progress,
        String created_at
) {
    public record PhaseDto(
            String id,
            String label,
            String title,
            List<LessonDto> lessons,
            boolean completed
    ) {}

    public record LessonDto(
            String id,
            String title,
            @JsonProperty("video_id")
            String videoId,
            int timestamp,
            @JsonProperty("duration_estimate")
            String durationEstimate,
            boolean completed
    ) {}
}
