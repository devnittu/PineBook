package com.pinebook.api.model;

import jakarta.persistence.*;

/**
 * A text chunk extracted from a video transcript, stored for reference.
 * Embeddings are stored exclusively in FAISS (not in PostgreSQL).
 */
@Entity
@Table(name = "chunks")
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Foreign key — YouTube video ID string (not the DB pk). */
    @Column(name = "video_id", nullable = false)
    private String videoId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    /** Start timestamp in seconds within the video. */
    @Column(nullable = false)
    private Integer timestamp;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Chunk() {}

    public Chunk(String videoId, String text, Integer timestamp) {
        this.videoId   = videoId;
        this.text      = text;
        this.timestamp = timestamp;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long    getId()        { return id; }
    public String  getVideoId()   { return videoId; }
    public String  getText()      { return text; }
    public Integer getTimestamp() { return timestamp; }

    public void setId(Long id)              { this.id        = id; }
    public void setVideoId(String videoId)  { this.videoId   = videoId; }
    public void setText(String text)        { this.text      = text; }
    public void setTimestamp(Integer ts)    { this.timestamp = ts; }
}
