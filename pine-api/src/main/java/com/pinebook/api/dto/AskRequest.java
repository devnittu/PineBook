package com.pinebook.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /ask
 */
public record AskRequest(

        @NotBlank(message = "Query must not be blank")
        @Size(min = 3, max = 500, message = "Query must be between 3 and 500 characters")
        String query
) {}
