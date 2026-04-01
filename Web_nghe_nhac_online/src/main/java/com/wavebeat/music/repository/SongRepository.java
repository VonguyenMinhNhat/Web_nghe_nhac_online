package com.wavebeat.music.repository;

import com.wavebeat.music.entity.AlbumEntity;
import com.wavebeat.music.entity.ArtistEntity;
import com.wavebeat.music.entity.GenreEntity;
import com.wavebeat.music.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<SongEntity, Long> {
    long countByArtist(ArtistEntity artist);
    long countByAlbum(AlbumEntity album);
    long countByGenre(GenreEntity genre);
}
