package com.wavebeat.music.repository;

import com.wavebeat.music.entity.ResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetTokenRepository extends JpaRepository<ResetTokenEntity, Long> {
    Optional<ResetTokenEntity> findTopByEmailOrderByCreatedAtDesc(String email);
    void deleteByEmail(String email);
}
