package com.wavebeat.music.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminGenreRequest(
    @NotBlank @Size(min = 1, max = 50, message = "Genre name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Genre name must contain only letters and spaces") 
    String name
) {
}
