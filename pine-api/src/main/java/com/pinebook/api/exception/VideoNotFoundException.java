package com.pinebook.api.exception;

/**
 * Thrown when a requested videoId does not exist in the database.
 */
public class VideoNotFoundException extends RuntimeException {

    public VideoNotFoundException(String videoId) {
        super("Video not found: " + videoId);
    }
}
