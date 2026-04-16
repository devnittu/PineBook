package com.pinebook.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Aggregated response body for POST /ask
 */
public record AskResponse(
        @JsonProperty("learning_path")
        List<LearningStep>        learningPath,

        @JsonProperty("best_explanations")
        Map<String, ChunkResult>  bestExplanations,

        List<ChunkResult>         confusions
) {

    public record LearningStep(
            String topic,
            ChunkResult result
    ) {}

    public record ChunkResult(
            String  text,
            int     timestamp,
            @JsonProperty("video_id")
            String  videoId
    ) {}
}
