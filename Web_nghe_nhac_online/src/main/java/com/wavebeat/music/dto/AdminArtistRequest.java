package com.wavebeat.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminArtistRequest(
    @NotBlank @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Artist name must contain only letters and spaces") String name,
    @NotBlank @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Country must contain only letters and spaces") String country,
    @NotBlank String imageUrl
) {
}
