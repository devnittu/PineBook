package com.pinebook.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Non-blocking HTTP client (WebClient) configuration for calling the Python AI service.
 */
@Configuration
public class WebClientConfig {

    @Value("${ai.service.base-url}")
    private String aiServiceBaseUrl;

    @Value("${ai.service.timeout-ms:5000}")
    private long timeoutMs;

    @Bean
    public WebClient aiServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(aiServiceBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
