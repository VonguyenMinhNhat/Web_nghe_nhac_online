package com.wavebeat.music.service;

import com.wavebeat.music.dto.AdminSongRequest;
import com.wavebeat.music.entity.AlbumEntity;
import com.wavebeat.music.entity.AppUserEntity;
import com.wavebeat.music.entity.ArtistEntity;
import com.wavebeat.music.entity.GenreEntity;
import com.wavebeat.music.entity.PlaylistEntity;
import com.wavebeat.music.entity.SongEntity;
import com.wavebeat.music.repository.AlbumRepository;
import com.wavebeat.music.repository.AppSessionRepository;
import com.wavebeat.music.repository.AppUserRepository;
import com.wavebeat.music.repository.ArtistRepository;
import com.wavebeat.music.repository.CommentRepository;
import com.wavebeat.music.repository.FavoriteRepository;
import com.wavebeat.music.repository.GenreRepository;
import com.wavebeat.music.repository.HistoryEntryRepository;
import com.wavebeat.music.repository.PlaylistRepository;
import com.wavebeat.music.repository.PlaylistSongRepository;
import com.wavebeat.music.repository.ResetTokenRepository;
import com.wavebeat.music.repository.SongRatingRepository;
import com.wavebeat.music.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MusicLibraryServiceTest {

    @Mock
    private AppUserRepository userRepository;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private SongRepository songRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private PlaylistSongRepository playlistSongRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private SongRatingRepository songRatingRepository;
    @Mock
    private HistoryEntryRepository historyEntryRepository;
    @Mock
    private ResetTokenRepository resetTokenRepository;
    @Mock
    private AppSessionRepository appSessionRepository;

    private MusicLibraryService musicLibraryService;

    @BeforeEach
    void setUp() {
        musicLibraryService = new MusicLibraryService(
            userRepository,
            artistRepository,
            genreRepository,
            albumRepository,
            songRepository,
            favoriteRepository,
            playlistRepository,
            playlistSongRepository,
            commentRepository,
            songRatingRepository,
            historyEntryRepository,
            resetTokenRepository,
            appSessionRepository
        );
    }

    @Test
    void shouldRejectDeletingArtistWhenAlbumsStillExist() {
        AppUserEntity admin = adminUser();
        ArtistEntity artist = new ArtistEntity();
        artist.setId(7L);

        mockCurrentUser(admin);
        when(artistRepository.findById(7L)).thenReturn(Optional.of(artist));
        when(albumRepository.countByArtist(artist)).thenReturn(1L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> musicLibraryService.deleteArtist(7L));

        assertEquals(400, exception.getStatusCode().value());
        verify(artistRepository, never()).delete(any());
    }

    @Test
    void shouldCascadeDeleteSongRelationsBeforeDeletingSong() {
        AppUserEntity admin = adminUser();
        SongEntity song = new SongEntity();
        song.setId(9L);

        mockCurrentUser(admin);
        when(songRepository.findById(9L)).thenReturn(Optional.of(song));

        assertEquals("Song deleted", musicLibraryService.deleteSong(9L).get("message"));

        verify(favoriteRepository).deleteBySong(song);
        verify(playlistSongRepository).deleteBySong(song);
        verify(commentRepository).deleteBySong(song);
        verify(songRatingRepository).deleteBySong(song);
        verify(historyEntryRepository).deleteBySong(song);
        verify(songRepository).delete(song);
    }

    @Test
    void shouldCreateSongUsingReferencedEntities() {
        AppUserEntity admin = adminUser();
        ArtistEntity artist = new ArtistEntity();
        artist.setId(2L);
        artist.setName("Neon Rivers");

        AlbumEntity album = new AlbumEntity();
        album.setId(3L);
        album.setTitle("Midnight");
        album.setArtist(artist);

        GenreEntity genre = new GenreEntity();
        genre.setId(5L);
        genre.setName("Synthwave");

        SongEntity saved = new SongEntity();
        saved.setId(11L);
        saved.setTitle("Signal Run");
        saved.setArtist(artist);
        saved.setAlbum(album);
        saved.setGenre(genre);
        saved.setDurationSeconds(240);
        saved.setPlays(0);
        saved.setTrending(true);
        saved.setAudioUrl("https://example.com/signal-run.mp3");

        mockCurrentUser(admin);
        when(artistRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(albumRepository.findById(3L)).thenReturn(Optional.of(album));
        when(genreRepository.findByNameIgnoreCase("Synthwave")).thenReturn(Optional.of(genre));
        when(songRepository.save(any(SongEntity.class))).thenReturn(saved);
        when(songRatingRepository.findBySong(saved)).thenReturn(List.of());
        when(playlistRepository.findByUser(admin)).thenReturn(List.of());
        when(favoriteRepository.existsByUserAndSong(admin, saved)).thenReturn(false);

        var response = musicLibraryService.createSong(new AdminSongRequest(
            "Signal Run",
            2L,
            3L,
            "Synthwave",
            240,
            0,
            true,
            "https://example.com/signal-run.mp3"
        ));

        assertEquals("Signal Run", response.title());
        assertEquals("Neon Rivers", response.artist());
        assertEquals("Synthwave", response.genre());
    }

    private void mockCurrentUser(AppUserEntity user) {
        var session = new com.wavebeat.music.entity.AppSessionEntity();
        session.setId(1L);
        session.setCurrentUser(user);
        when(appSessionRepository.findById(1L)).thenReturn(Optional.of(session));
    }

    private AppUserEntity adminUser() {
        AppUserEntity user = new AppUserEntity();
        user.setId(1L);
        user.setRoleName("ADMIN");
        user.setUsername("admin");
        user.setFullName("Administrator");
        user.setEmail("demo@wavebeat.local");
        user.setPassword("demo123");
        user.setLocked(false);
        return user;
    }
}
