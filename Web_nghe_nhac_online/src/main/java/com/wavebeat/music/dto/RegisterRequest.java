package com.wavebeat.music.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,
    @NotBlank @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters") String fullName,
    @Email @NotBlank String email,
    @NotBlank @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters") String password
) {
}
