package com.pinebook.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body for GET /api/progress
 */
public record ProgressResponse(
        @JsonProperty("total_paths")
        int totalPaths,

        @JsonProperty("completed_paths")
        int completedPaths,

        @JsonProperty("total_lessons")
        int totalLessons,

        @JsonProperty("completed_lessons")
        int completedLessons,

        @JsonProperty("streak_days")
        int streakDays,

        @JsonProperty("last_active")
        String lastActive
) {}
