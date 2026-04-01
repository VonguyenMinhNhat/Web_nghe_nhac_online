package com.wavebeat.music.repository;

import com.wavebeat.music.entity.AppSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSessionRepository extends JpaRepository<AppSessionEntity, Long> {
}
