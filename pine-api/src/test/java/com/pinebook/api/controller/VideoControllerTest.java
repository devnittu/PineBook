package com.pinebook.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinebook.api.dto.AddVideoRequest;
import com.pinebook.api.dto.AddVideoResponse;
import com.pinebook.api.dto.StatusResponse;
import com.pinebook.api.exception.VideoNotFoundException;
import com.pinebook.api.service.VideoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for VideoController.
 * Only loads the web layer — no DB, no AI service.
 */
@WebMvcTest(VideoController.class)
class VideoControllerTest {

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean VideoService videoService;

    // ── POST /add-video ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /add-video: valid URL returns 202 ACCEPTED")
    void addVideo_validUrl_returns202() throws Exception {
        when(videoService.addVideo(any()))
                .thenReturn(new AddVideoResponse("dQw4w9WgXcQ", "processing"));

        mockMvc.perform(post("/add-video")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new AddVideoRequest("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.videoId").value("dQw4w9WgXcQ"))
                .andExpect(jsonPath("$.status").value("processing"));
    }

    @Test
    @DisplayName("POST /add-video: blank URL returns 400 BAD REQUEST")
    void addVideo_blankUrl_returns400() throws Exception {
        mockMvc.perform(post("/add-video")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /add-video: invalid URL format returns 400 BAD REQUEST")
    void addVideo_invalidUrl_returns400() throws Exception {
        mockMvc.perform(post("/add-video")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://vimeo.com/123456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /add-video: missing body returns 400")
    void addVideo_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/add-video")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── GET /status/{videoId} ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /status/{videoId}: known video returns status")
    void getStatus_knownVideo_returns200() throws Exception {
        when(videoService.getStatus("dQw4w9WgXcQ"))
                .thenReturn(new StatusResponse("dQw4w9WgXcQ", "done"));

        mockMvc.perform(get("/status/dQw4w9WgXcQ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoId").value("dQw4w9WgXcQ"))
                .andExpect(jsonPath("$.status").value("done"));
    }

    @Test
    @DisplayName("GET /status/{videoId}: unknown video returns 404")
    void getStatus_unknownVideo_returns404() throws Exception {
        when(videoService.getStatus("notexist0"))
                .thenThrow(new VideoNotFoundException("notexist0"));

        mockMvc.perform(get("/status/notexist0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VIDEO_NOT_FOUND"));
    }
}
