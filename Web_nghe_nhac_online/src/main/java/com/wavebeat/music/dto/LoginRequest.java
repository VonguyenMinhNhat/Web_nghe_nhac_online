package com.wavebeat.music.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @Email @NotBlank String email,
    @NotBlank @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password
) {
}
