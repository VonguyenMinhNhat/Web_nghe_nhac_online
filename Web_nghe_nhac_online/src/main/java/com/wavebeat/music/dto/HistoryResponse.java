package com.wavebeat.music.dto;

public record HistoryResponse(
    Long id,
    Long songId,
    String songTitle,
    String artist,
    String playedAt
) {
}
