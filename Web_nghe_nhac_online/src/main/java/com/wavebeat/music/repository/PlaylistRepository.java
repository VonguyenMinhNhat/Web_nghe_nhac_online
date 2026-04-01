package com.wavebeat.music.repository;

import com.wavebeat.music.entity.AppUserEntity;
import com.wavebeat.music.entity.PlaylistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Long> {
    List<PlaylistEntity> findByUser(AppUserEntity user);
}
