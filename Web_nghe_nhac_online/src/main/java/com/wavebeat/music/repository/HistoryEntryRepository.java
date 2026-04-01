package com.wavebeat.music.repository;

import com.wavebeat.music.entity.AppUserEntity;
import com.wavebeat.music.entity.HistoryEntryEntity;
import com.wavebeat.music.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryEntryRepository extends JpaRepository<HistoryEntryEntity, Long> {
    List<HistoryEntryEntity> findByUserOrderByPlayedAtDesc(AppUserEntity user);
    void deleteByUser(AppUserEntity user);
    long countBySong(SongEntity song);
    void deleteBySong(SongEntity song);
}
