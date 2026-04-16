package com.pinebook.api.service;

import com.pinebook.api.dto.AddVideoRequest;
import com.pinebook.api.dto.AddVideoResponse;
import com.pinebook.api.dto.StatusResponse;
import com.pinebook.api.exception.VideoNotFoundException;
import com.pinebook.api.model.Video;
import com.pinebook.api.model.VideoStatus;
import com.pinebook.api.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles video submission, async processing dispatch, and status queries.
 *
 * Key design rules:
 *  - addVideo() returns immediately (never blocks on AI processing)
 *  - triggerAsyncProcessing() runs on "videoProcessingExecutor" thread pool
 *  - Status is always updated in DB — even on failure
 */
@Service
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    private final VideoRepository videoRepository;
    private final AIClientService aiClientService;

    public VideoService(VideoRepository videoRepository, AIClientService aiClientService) {
        this.videoRepository = videoRepository;
        this.aiClientService = aiClientService;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    @Transactional
    public AddVideoResponse addVideo(AddVideoRequest request) {
        String videoId = extractVideoId(request.url());
        log.info("addVideo videoId={}", videoId);

        // Idempotent: already queued/processed
        if (videoRepository.existsByVideoId(videoId)) {
            Video existing = videoRepository.findByVideoId(videoId).orElseThrow();
            log.info("Video already indexed videoId={} status={}", videoId, existing.getStatus());
            return new AddVideoResponse(videoId, existing.getStatus().name().toLowerCase());
        }

        Video video = Video.builder()
                .videoId(videoId)
                .status(VideoStatus.PROCESSING)
                .build();
        videoRepository.save(video);
        log.info("Video record created videoId={}", videoId);

        // Fire-and-forget: kicks off async thread, returns immediately
        triggerAsyncProcessing(videoId);

        return new AddVideoResponse(videoId, VideoStatus.PROCESSING.name().toLowerCase());
    }

    public StatusResponse getStatus(String videoId) {
        Video video = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId));
        return new StatusResponse(videoId, video.getStatus().name().toLowerCase());
    }

    // ── Async processing ─────────────────────────────────────────────────────

    /**
     * Runs on the dedicated async thread pool.
     * Calls Python service; updates DB status on success or failure.
     * Never propagates exceptions — system must not crash.
     */
    @Async("videoProcessingExecutor")
    public void triggerAsyncProcessing(String videoId) {
        log.info("Async processing started videoId={}", videoId);
        try {
            aiClientService.processVideo(videoId);
            updateStatus(videoId, VideoStatus.DONE);
            log.info("Async processing SUCCESS videoId={}", videoId);
        } catch (Exception e) {
            log.error("Async processing FAILED videoId={} error={}", videoId, e.getMessage(), e);
            updateStatus(videoId, VideoStatus.FAILED);
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    @Transactional
    public void updateStatus(String videoId, VideoStatus status) {
        int rows = videoRepository.updateStatusByVideoId(status, videoId);
        if (rows == 0) {
            log.warn("updateStatus: no row updated for videoId={}", videoId);
        }
    }

    /**
     * Extracts the 11-character video ID from standard YouTube URL formats.
     *
     * Supported:
     *   https://www.youtube.com/watch?v=dQw4w9WgXcQ
     *   https://youtu.be/dQw4w9WgXcQ
     *   https://youtube.com/watch?v=dQw4w9WgXcQ&feature=share
     */
    String extractVideoId(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL must not be blank");
        }
        String id;
        if (url.contains("youtu.be/")) {
            id = url.split("youtu\\.be/")[1].split("[?&#]")[0];
        } else if (url.contains("v=")) {
            id = url.split("[?&]v=")[1].split("[?&#]")[0];
        } else {
            throw new IllegalArgumentException("Cannot extract video ID from URL: " + url);
        }
        if (id.length() < 11) {
            throw new IllegalArgumentException("Extracted video ID looks invalid: " + id);
        }
        return id.substring(0, 11);
    }
}
