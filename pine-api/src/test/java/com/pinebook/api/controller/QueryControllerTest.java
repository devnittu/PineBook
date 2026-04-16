package com.pinebook.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinebook.api.dto.AskRequest;
import com.pinebook.api.dto.AskResponse;
import com.pinebook.api.service.QueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for QueryController.
 */
@WebMvcTest(QueryController.class)
class QueryControllerTest {

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean QueryService queryService;

    private static final AskResponse STUB_RESPONSE = new AskResponse(
            List.of(new AskResponse.LearningStep("basics",
                    new AskResponse.ChunkResult("intro text", 0, "vid1"))),
            Map.of("Basics", new AskResponse.ChunkResult("short text", 30, "vid1")),
            List.of(new AskResponse.ChunkResult("supervised vs", 60, "vid2"))
    );

    // ── POST /ask ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /ask: valid query returns 200 with structured response")
    void ask_validQuery_returns200() throws Exception {
        when(queryService.ask(any())).thenReturn(STUB_RESPONSE);

        mockMvc.perform(post("/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new AskRequest("learn machine learning"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.learning_path").isArray())
                .andExpect(jsonPath("$.best_explanations").isMap())
                .andExpect(jsonPath("$.confusions").isArray());
    }

    @Test
    @DisplayName("POST /ask: blank query returns 400")
    void ask_blankQuery_returns400() throws Exception {
        mockMvc.perform(post("/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /ask: too-short query returns 400")
    void ask_tooShortQuery_returns400() throws Exception {
        mockMvc.perform(post("/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"ab\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /ask: missing body returns 400")
    void ask_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/ask").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
