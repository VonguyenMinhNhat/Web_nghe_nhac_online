package com.wavebeat.music.repository;

import com.wavebeat.music.entity.CommentEntity;
import com.wavebeat.music.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findBySongOrderByCreatedAtDesc(SongEntity song);
    long countBySong(SongEntity song);
    void deleteBySong(SongEntity song);
}
