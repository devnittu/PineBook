package com.pinebook.api.controller;

import com.pinebook.api.dto.*;
import com.pinebook.api.service.QueryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Thin controller — delegates all AI orchestration to QueryService.
 */
@RestController
@RequestMapping("/api")
public class QueryController {

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * POST /api/query
     * Validates query, calls Python AI service, returns structured learning path.
     */
    @PostMapping("/query")
    public ResponseEntity<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        log.info("POST /api/query query=\"{}\"", request.query());
        AskResponse response = queryService.ask(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/chat — conversational follow-up
     * [STUB] Returns empty responses
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody Map<String, Object> request) {
        log.info("POST /api/chat message=\"{}\"", request.get("message"));
        ChatResponse response = new ChatResponse("Follow-up support coming soon.");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/history — get query history
     * [STUB] Returns empty list
     */
    @GetMapping("/history")
    public ResponseEntity<List<HistoryItemDto>> getHistory() {
        log.info("GET /api/history");
        return ResponseEntity.ok(List.of());
    }

    /**
     * GET /api/path/:id — get learning path details
     * [STUB] Returns empty path
     */
    @GetMapping("/path/{id}")
    public ResponseEntity<LearningPathDto> getPath(@PathVariable String id) {
        log.info("GET /api/path/{}", id);
        LearningPathDto response = new LearningPathDto(id, "Learning Path", List.of(), 0, "2024-01-01");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/progress — get user progress
     * [STUB] Returns zero progress
     */
    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> getProgress() {
        log.info("GET /api/progress");
        ProgressResponse response = new ProgressResponse(0, 0, 0, 0, 0, "never");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/confusion — resolve confusion concept
     * [STUB] Returns generic explanation
     */
    @PostMapping("/confusion")
    public ResponseEntity<ConfusionResolverResponse> resolveConfusion(@RequestBody Map<String, String> request) {
        String concept = request.getOrDefault("concept", "");
        log.info("POST /api/confusion concept=\"{}\"", concept);
        ConfusionResolverResponse response = new ConfusionResolverResponse(
                concept,
                "AI service will provide detailed explanation soon.",
                "",
                List.of()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/recommendations — get personalized recommendations
     * [STUB] Returns empty list
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDto>> getRecommendations() {
        log.info("GET /api/recommendations");
        return ResponseEntity.ok(List.of());
    }
}
