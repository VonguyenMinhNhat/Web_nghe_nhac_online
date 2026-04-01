package com.wavebeat.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record AdminSongRequest(
    @NotBlank String title,
    @NotNull Long artistId,
    @NotNull Long albumId,
    @NotBlank String genre,
    @Min(value = 30, message = "Duration must be at least 30 seconds") int durationSeconds,
    @Min(value = 0, message = "Plays cannot be negative") long plays,
    boolean trending,
    @NotBlank String audioUrl
) {
}
