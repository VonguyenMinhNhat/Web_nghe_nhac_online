package com.wavebeat.music.model;

public record Album(
    Long id,
    String title,
    Long artistId,
    int releaseYear,
    String coverColor
) {
}
