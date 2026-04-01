package com.wavebeat.music.repository;

import com.wavebeat.music.entity.AppUserEntity;
import com.wavebeat.music.entity.SongEntity;
import com.wavebeat.music.entity.SongRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongRatingRepository extends JpaRepository<SongRatingEntity, Long> {
    List<SongRatingEntity> findBySong(SongEntity song);
    Optional<SongRatingEntity> findBySongAndUser(SongEntity song, AppUserEntity user);
    long countBySong(SongEntity song);
    void deleteBySong(SongEntity song);
}
