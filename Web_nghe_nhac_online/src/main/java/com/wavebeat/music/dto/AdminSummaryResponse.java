package com.wavebeat.music.dto;

public record AdminSummaryResponse(
    int totalUsers,
    int totalSongs,
    int totalArtists,
    int totalAlbums,
    int totalGenres
) {
}
