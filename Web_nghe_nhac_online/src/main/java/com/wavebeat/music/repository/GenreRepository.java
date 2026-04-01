package com.wavebeat.music.repository;

import com.wavebeat.music.entity.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<GenreEntity, Long> {
    Optional<GenreEntity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
