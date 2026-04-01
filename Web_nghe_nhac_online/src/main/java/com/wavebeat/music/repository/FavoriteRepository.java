package com.wavebeat.music.repository;

import com.wavebeat.music.entity.AppUserEntity;
import com.wavebeat.music.entity.FavoriteEntity;
import com.wavebeat.music.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    List<FavoriteEntity> findByUser(AppUserEntity user);
    Optional<FavoriteEntity> findByUserAndSong(AppUserEntity user, SongEntity song);
    boolean existsByUserAndSong(AppUserEntity user, SongEntity song);
    long countBySong(SongEntity song);
    void deleteBySong(SongEntity song);
}
