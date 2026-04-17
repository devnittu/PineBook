package com.pinebook.api.dto;

import java.util.List;

/**
 * Response body for POST /api/confusion
 */
public record ConfusionResolverResponse(
        String concept,
        String explanation,
        String analogy,
        List<String> related
) {}
