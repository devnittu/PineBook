package com.pinebook.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST /add-video
 */
public record AddVideoRequest(

        @NotBlank(message = "YouTube URL must not be blank")
        @Pattern(
                regexp = "^https?://(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]{11}.*$",
                message = "Invalid YouTube URL format"
        )
        String url
) {}
