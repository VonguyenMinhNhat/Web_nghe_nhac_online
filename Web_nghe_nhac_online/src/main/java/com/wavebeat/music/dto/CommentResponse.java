package com.wavebeat.music.dto;

public record CommentResponse(
    Long id,
    Long songId,
    String userName,
    String message,
    int rating,
    String createdAt
) {
}
