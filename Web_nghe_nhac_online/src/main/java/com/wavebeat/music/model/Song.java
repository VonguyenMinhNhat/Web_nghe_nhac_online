package com.wavebeat.music.model;

public record Song(
    Long id,
    String title,
    Long artistId,
    Long albumId,
    String genre,
    int durationSeconds,
    long plays,
    boolean trending,
    String audioUrl
) {
}
