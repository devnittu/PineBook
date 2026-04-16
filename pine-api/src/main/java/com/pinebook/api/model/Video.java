package com.pinebook.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a YouTube video submitted for AI processing.
 */
@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** YouTube video ID (e.g. "dQw4w9WgXcQ"). */
    @Column(name = "video_id", nullable = false, unique = true)
    private String videoId;

    /** Processing lifecycle: pending → processing → done | failed */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Video() {}

    public Video(String videoId, VideoStatus status) {
        this.videoId = videoId;
        this.status  = status;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String      videoId;
        private VideoStatus status;

        public Builder videoId(String v)      { this.videoId = v; return this; }
        public Builder status(VideoStatus s)  { this.status  = s; return this; }

        public Video build() {
            Video v = new Video();
            v.videoId = this.videoId;
            v.status  = this.status;
            return v;
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long          getId()        { return id; }
    public String        getVideoId()   { return videoId; }
    public VideoStatus   getStatus()    { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id)              { this.id     = id; }
    public void setVideoId(String videoId)  { this.videoId = videoId; }
    public void setStatus(VideoStatus s)    { this.status  = s; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
    public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
}
