package com.wavebeat.music.dto;

import java.util.Set;

public record UserResponse(
    Long id,
    String username,
    String fullName,
    String email,
    String role,
    boolean locked,
    Set<Long> favoriteSongIds
) {
}
