package com.wavebeat.music.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
    @NotBlank String message,
    @Min(1) @Max(5) int rating
) {
}
