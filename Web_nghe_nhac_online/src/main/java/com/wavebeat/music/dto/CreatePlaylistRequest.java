package com.wavebeat.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlaylistRequest(
    @NotBlank(message = "Playlist name is required")
    @Size(max = 50, message = "Playlist name must be at most 50 characters")
    String name
) {
}
