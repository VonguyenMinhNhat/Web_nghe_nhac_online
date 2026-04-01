package com.wavebeat.music.dto;

public record AuthResponse(
    String message,
    UserResponse user
) {
}
