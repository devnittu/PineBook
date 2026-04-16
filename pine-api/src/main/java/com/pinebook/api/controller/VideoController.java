package com.pinebook.api.controller;

import com.pinebook.api.dto.AddVideoRequest;
import com.pinebook.api.dto.AddVideoResponse;
import com.pinebook.api.dto.StatusResponse;
import com.pinebook.api.service.VideoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Thin controller — delegates all logic to VideoService.
 * Responsible only for HTTP binding and status codes.
 */
@RestController
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * POST /add-video
     * Accepts a YouTube URL, stores a DB record, triggers async AI processing.
     * Returns immediately (non-blocking).
     */
    @PostMapping("/add-video")
    public ResponseEntity<AddVideoResponse> addVideo(@Valid @RequestBody AddVideoRequest request) {
        log.info("POST /add-video url={}", request.url());
        AddVideoResponse response = videoService.addVideo(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * GET /status/{videoId}
     * Returns current processing status of a video.
     */
    @GetMapping("/status/{videoId}")
    public ResponseEntity<StatusResponse> getStatus(@PathVariable String videoId) {
        log.info("GET /status/{}", videoId);
        StatusResponse response = videoService.getStatus(videoId);
        return ResponseEntity.ok(response);
    }
}
