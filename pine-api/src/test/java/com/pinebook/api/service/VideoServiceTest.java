package com.pinebook.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Pure unit tests for VideoService — no Spring context needed.
 * Focuses on the deterministic URL extraction logic.
 */
class VideoServiceTest {

    private VideoService videoService;

    @BeforeEach
    void setUp() {
        // Mocks for dependencies; extractVideoId has no dependencies
        videoService = new VideoService(mock(com.pinebook.api.repository.VideoRepository.class),
                                        mock(AIClientService.class));
    }

    // ── extractVideoId happy path ─────────────────────────────────────────────

    @ParameterizedTest(name = "[{index}] {0} → {1}")
    @DisplayName("extractVideoId: valid URL formats")
    @CsvSource({
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ,              dQw4w9WgXcQ",
            "https://youtube.com/watch?v=dQw4w9WgXcQ,                  dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&feature=share,dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ,                             dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?si=abc123,                   dQw4w9WgXcQ",
    })
    void extractVideoId_validUrls(String url, String expectedId) {
        String actualId = videoService.extractVideoId(url.trim());
        assertThat(actualId).isEqualTo(expectedId.trim());
    }

    // ── extractVideoId error cases ────────────────────────────────────────────

    @Test
    @DisplayName("extractVideoId: blank URL throws IllegalArgumentException")
    void extractVideoId_blankUrl_throws() {
        assertThatThrownBy(() -> videoService.extractVideoId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    @DisplayName("extractVideoId: URL with no video ID param throws IllegalArgumentException")
    void extractVideoId_noParam_throws() {
        assertThatThrownBy(() -> videoService.extractVideoId("https://youtube.com/"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot extract");
    }

    @Test
    @DisplayName("extractVideoId: short ID throws IllegalArgumentException")
    void extractVideoId_shortId_throws() {
        assertThatThrownBy(() -> videoService.extractVideoId("https://youtube.com/watch?v=abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }

    // ── extractVideoId always returns exactly 11 chars ────────────────────────

    @Test
    @DisplayName("extractVideoId: always returns exactly 11 characters")
    void extractVideoId_always11Chars() {
        String id = videoService.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42s");
        assertThat(id).hasSize(11);
    }
}
