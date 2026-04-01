package com.wavebeat.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record AdminAlbumRequest(
    @NotBlank String title,
    @NotNull Long artistId,
    @Min(value = 1900, message = "Release year must be at least 1900") 
    @Max(value = 2100, message = "Release year cannot exceed 2100") 
    int releaseYear,
    @NotBlank String coverColor
) {
}
