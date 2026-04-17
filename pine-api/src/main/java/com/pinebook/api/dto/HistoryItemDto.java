package com.pinebook.api.dto;

/**
 * Response item for GET /api/history
 */
public record HistoryItemDto(
        String id,
        String query,
        String created_at,
        String path_id
) {}
