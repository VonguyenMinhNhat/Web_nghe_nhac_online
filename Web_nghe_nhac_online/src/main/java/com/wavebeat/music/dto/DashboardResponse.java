package com.wavebeat.music.dto;

import java.util.List;

public record DashboardResponse(
    int totalSongs,
    int totalFavorites,
    int totalPlaylistEntries,
    List<String> genres,
    SongResponse heroSong
) {
}
