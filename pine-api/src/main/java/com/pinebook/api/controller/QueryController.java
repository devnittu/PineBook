package com.pinebook.api.controller;

import com.pinebook.api.dto.AskRequest;
import com.pinebook.api.dto.AskResponse;
import com.pinebook.api.service.QueryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Thin controller — delegates all AI orchestration to QueryService.
 */
@RestController
public class QueryController {

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * POST /ask
     * Validates query, calls Python AI service, returns structured learning path.
     */
    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        log.info("POST /ask query=\"{}\"", request.query());
        AskResponse response = queryService.ask(request);
        return ResponseEntity.ok(response);
    }
}
