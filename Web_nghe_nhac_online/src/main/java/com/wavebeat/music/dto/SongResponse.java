package com.wavebeat.music.dto;

public record SongResponse(
    Long id,
    String title,
    String artist,
    String album,
    String genre,
    int durationSeconds,
    String durationLabel,
    long plays,
    boolean trending,
    boolean favorite,
    boolean inAnyPlaylist,
    String audioUrl,
    double averageRating,
    int totalRatings
) {
}
