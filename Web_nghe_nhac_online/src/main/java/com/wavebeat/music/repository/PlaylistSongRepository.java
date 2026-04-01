package com.wavebeat.music.repository;

import com.wavebeat.music.entity.PlaylistEntity;
import com.wavebeat.music.entity.PlaylistSongEntity;
import com.wavebeat.music.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSongEntity, Long> {
    List<PlaylistSongEntity> findByPlaylist(PlaylistEntity playlist);
    Optional<PlaylistSongEntity> findByPlaylistAndSong(PlaylistEntity playlist, SongEntity song);
    boolean existsByPlaylistAndSong(PlaylistEntity playlist, SongEntity song);
    long countBySong(SongEntity song);
    void deleteBySong(SongEntity song);
}
