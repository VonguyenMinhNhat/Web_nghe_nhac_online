package com.wavebeat.music.model;

import java.util.List;
import java.util.Set;

public record UserProfile(
    Long id,
    String username,
    String fullName,
    String email,
    Set<Long> favoriteSongIds,
    List<Playlist> playlists
) {
}
