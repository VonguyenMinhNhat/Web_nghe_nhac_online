package com.wavebeat.music.model;

import java.util.List;

public record Playlist(
    Long id,
    String name,
    List<Long> songIds
) {
}
