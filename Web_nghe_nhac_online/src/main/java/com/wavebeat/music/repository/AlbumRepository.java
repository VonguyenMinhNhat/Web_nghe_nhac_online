package com.wavebeat.music.repository;

import com.wavebeat.music.entity.ArtistEntity;
import com.wavebeat.music.entity.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {
    long countByArtist(ArtistEntity artist);
}
