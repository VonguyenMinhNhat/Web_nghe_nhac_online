package com.wavebeat.music.model;

public record Artist(
    Long id,
    String name,
    String country,
    String imageUrl
) {
}
