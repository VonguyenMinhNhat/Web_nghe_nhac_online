package com.wavebeat.music.dto;

import java.util.List;

public record PlaylistResponse(
    Long id,
    String name,
    int totalSongs,
    List<Long> songIds
) {
}
