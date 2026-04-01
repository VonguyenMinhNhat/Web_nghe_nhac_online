package com.wavebeat.music.service;

import com.wavebeat.music.dto.AdminAlbumRequest;
import com.wavebeat.music.dto.AdminArtistRequest;
import com.wavebeat.music.dto.AdminGenreRequest;
import com.wavebeat.music.dto.AdminSongRequest;
import com.wavebeat.music.dto.AdminSummaryResponse;
import com.wavebeat.music.dto.AuthResponse;
import com.wavebeat.music.dto.ChangePasswordRequest;
import com.wavebeat.music.dto.CommentRequest;
import com.wavebeat.music.dto.CommentResponse;
import com.wavebeat.music.dto.DashboardResponse;
import com.wavebeat.music.dto.ForgotPasswordRequest;
import com.wavebeat.music.dto.HistoryResponse;
import com.wavebeat.music.dto.LoginRequest;
import com.wavebeat.music.dto.PlaylistResponse;
import com.wavebeat.music.dto.RatingRequest;
import com.wavebeat.music.dto.RegisterRequest;
import com.wavebeat.music.dto.ResetPasswordRequest;
import com.wavebeat.music.dto.SongResponse;
import com.wavebeat.music.dto.UpdateProfileRequest;
import com.wavebeat.music.dto.UserResponse;
import com.wavebeat.music.entity.AlbumEntity;
import com.wavebeat.music.entity.AppSessionEntity;
import com.wavebeat.music.entity.AppUserEntity;
import com.wavebeat.music.entity.ArtistEntity;
import com.wavebeat.music.entity.CommentEntity;
import com.wavebeat.music.entity.FavoriteEntity;
import com.wavebeat.music.entity.GenreEntity;
import com.wavebeat.music.entity.HistoryEntryEntity;
import com.wavebeat.music.entity.PlaylistEntity;
import com.wavebeat.music.entity.PlaylistSongEntity;
import com.wavebeat.music.entity.ResetTokenEntity;
import com.wavebeat.music.entity.SongEntity;
import com.wavebeat.music.entity.SongRatingEntity;
import com.wavebeat.music.model.Album;
import com.wavebeat.music.model.Artist;
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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class MusicLibraryService {

    private static final Logger logger = LoggerFactory.getLogger(MusicLibraryService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Long CURRENT_SESSION_ID = 1L;

    private final AppUserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final FavoriteRepository favoriteRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final CommentRepository commentRepository;
    private final SongRatingRepository songRatingRepository;
    private final HistoryEntryRepository historyEntryRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final AppSessionRepository appSessionRepository;

    public MusicLibraryService(
        AppUserRepository userRepository,
        ArtistRepository artistRepository,
        GenreRepository genreRepository,
        AlbumRepository albumRepository,
        SongRepository songRepository,
        FavoriteRepository favoriteRepository,
        PlaylistRepository playlistRepository,
        PlaylistSongRepository playlistSongRepository,
        CommentRepository commentRepository,
        SongRatingRepository songRatingRepository,
        HistoryEntryRepository historyEntryRepository,
        ResetTokenRepository resetTokenRepository,
        AppSessionRepository appSessionRepository
    ) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.genreRepository = genreRepository;
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
        this.favoriteRepository = favoriteRepository;
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.commentRepository = commentRepository;
        this.songRatingRepository = songRatingRepository;
        this.historyEntryRepository = historyEntryRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.appSessionRepository = appSessionRepository;
    }

    public DashboardResponse getDashboard() {
        try {
            AppUserEntity user = currentUserOrNull();
            SongEntity hero = songRepository.findAll().stream()
                .max(Comparator.comparingLong(SongEntity::getPlays))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No songs available"));

            int favorites = user == null ? 0 : favoriteRepository.findByUser(user).size();
            int playlistEntries = user == null ? 0 : playlistRepository.findByUser(user).stream()
                .mapToInt(playlist -> playlistSongRepository.findByPlaylist(playlist).size())
                .sum();

            logger.info("Dashboard retrieved for user: {}", user == null ? "anonymous" : user.getUsername());
            return new DashboardResponse(
                (int) songRepository.count(),
                favorites,
                playlistEntries,
                getGenres(),
                toSongResponse(hero, user)
            );
        } catch (Exception e) {
            logger.error("Error retrieving dashboard", e);
            throw e;
        }
    }

    @Transactional
    public List<SongResponse> getSongs(String search, String genre, String filter) {
        String searchText = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String genreText = genre == null ? "all" : genre.trim().toLowerCase(Locale.ROOT);
        String filterText = filter == null ? "all" : filter.trim().toLowerCase(Locale.ROOT);
        AppUserEntity user = currentUserOrNull();
        Set<Long> favoriteIds = user == null ? Set.of() : favoriteSongIds(user);
        Set<Long> playlistSongIds = user == null ? Set.of() : playlistSongIds(user);

        List<SongResponse> songs = songRepository.findAll().stream()
            .filter(song -> searchText.isBlank() || matchesSearch(song, searchText))
            .filter(song -> "all".equals(genreText) || song.getGenre().getName().equalsIgnoreCase(genreText))
            .filter(song -> switch (filterText) {
                case "trending" -> song.isTrending();
                case "favorites" -> favoriteIds.contains(song.getId());
                case "playlist" -> playlistSongIds.contains(song.getId());
                default -> true;
            })
            .sorted(Comparator.comparingLong(SongEntity::getPlays).reversed())
            .map(song -> toSongResponse(song, user))
            .toList();
        
        logger.debug("Retrieved {} songs (search: {}, genre: {}, filter: {})", songs.size(), searchText, genreText, filterText);
        return songs;
    }

    @Transactional
    public SongResponse getSong(Long songId) {
        if (songId == null || songId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid song ID");
        }
        return toSongResponse(requireSong(songId), currentUserOrNull());
    }

    @Transactional
    public List<String> getGenres() {
        List<String> genres = genreRepository.findAll().stream()
            .map(GenreEntity::getName)
            .sorted()
            .toList();
        logger.debug("Retrieved {} genres", genres.size());
        return genres;
    }

    @Transactional
    public List<Artist> getArtists() {
        List<Artist> artists = artistRepository.findAll().stream()
            .map(artist -> new Artist(artist.getId(), artist.getName(), artist.getCountry(), artist.getImageUrl()))
            .toList();
        logger.debug("Retrieved {} artists", artists.size());
        return artists;
    }

    @Transactional
    public List<Album> getAlbums() {
        List<Album> albums = albumRepository.findAll().stream()
            .map(album -> new Album(album.getId(), album.getTitle(), album.getArtist().getId(), album.getReleaseYear(), album.getCoverColor()))
            .toList();
        logger.debug("Retrieved {} albums", albums.size());
        return albums;
    }

    @Transactional
    public UserResponse getCurrentUser() {
        return toUserResponse(requireCurrentUser());
    }

    public AuthResponse register(RegisterRequest request) {
        if (request == null || request.email() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            logger.warn("Registration attempt with existing email: {}", request.email());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            logger.warn("Registration attempt with existing username: {}", request.username());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        AppUserEntity user = new AppUserEntity();
        user.setUsername(request.username().trim());
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        user.setPassword(request.password());
        user.setRoleName("USER");
        user.setLocked(false);
        user = userRepository.save(user);
        setCurrentUser(user);
        logger.info("User registered successfully: {}", user.getUsername());
        return new AuthResponse("Registered successfully", toUserResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || request.email() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        AppUserEntity user = userRepository.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() -> {
                logger.warn("Login attempt with non-existent email: {}", request.email());
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
            });
        if (user.isLocked()) {
            logger.warn("Login attempt for locked account: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is locked");
        }
        if (!user.getPassword().equals(request.password())) {
            logger.warn("Login attempt with incorrect password for user: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }
        setCurrentUser(user);
        logger.info("User logged in successfully: {}", user.getUsername());
        return new AuthResponse("Login successful", toUserResponse(user));
    }

    public AuthResponse logout() {
        AppUserEntity currentUser = currentUserOrNull();
        setCurrentUser(null);
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
        }
        return new AuthResponse("Logout successful", null);
    }

    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        if (request == null || request.email() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        AppUserEntity user = userRepository.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() -> {
                logger.warn("Forgot password request for non-existent email: {}", request.email());
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
            });
        resetTokenRepository.deleteByEmail(user.getEmail());
        ResetTokenEntity token = new ResetTokenEntity();
        token.setEmail(user.getEmail());
        token.setResetCode("RESET-" + user.getId());
        token.setCreatedAt(LocalDateTime.now());
        resetTokenRepository.save(token);
        logger.info("Reset token generated for user: {}", user.getUsername());
        return Map.of("message", "Reset code generated", "resetCode", token.getResetCode());
    }

    public AuthResponse resetPassword(ResetPasswordRequest request) {
        if (request == null || request.email() == null || request.resetCode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        ResetTokenEntity token = resetTokenRepository.findTopByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> {
                logger.warn("Password reset failed - no token for email: {}", email);
                return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset code");
            });
        if (!token.getResetCode().equals(request.resetCode().trim())) {
            logger.warn("Password reset failed - invalid reset code for email: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset code");
        }
        AppUserEntity user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        user.setPassword(request.newPassword());
        userRepository.save(user);
        resetTokenRepository.deleteByEmail(email);
        setCurrentUser(user);
        logger.info("Password reset successful for user: {}", user.getUsername());
        return new AuthResponse("Password reset successful", toUserResponse(user));
    }

    public UserResponse updateProfile(UpdateProfileRequest request) {
        if (request == null || request.email() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        AppUserEntity user = requireCurrentUser();
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), user.getId())) {
            logger.warn("Profile update failed - email already exists: {}", request.email());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(request.username(), user.getId())) {
            logger.warn("Profile update failed - username already exists: {}", request.username());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        user.setUsername(request.username().trim());
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        userRepository.save(user);
        logger.info("User profile updated: {}", user.getUsername());
        return toUserResponse(user);
    }

    public AuthResponse changePassword(ChangePasswordRequest request) {
        if (request == null || request.currentPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        AppUserEntity user = requireCurrentUser();
        if (!user.getPassword().equals(request.currentPassword())) {
            logger.warn("Password change failed - incorrect current password for user: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPassword(request.newPassword());
        userRepository.save(user);
        logger.info("Password changed for user: {}", user.getUsername());
        return new AuthResponse("Password changed", toUserResponse(user));
    }

    @Transactional
    public Set<Long> getFavorites() {
        return favoriteSongIds(requireCurrentUser());
    }

    public void addFavorite(Long songId) {
        if (songId == null || songId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid song ID");
        }
        AppUserEntity user = requireCurrentUser();
        SongEntity song = requireSong(songId);
        if (!favoriteRepository.existsByUserAndSong(user, song)) {
            FavoriteEntity favorite = new FavoriteEntity();
            favorite.setUser(user);
            favorite.setSong(song);
            favoriteRepository.save(favorite);
            logger.info("Added favorite song {} for user {}", songId, user.getUsername());
        }
    }

    public void removeFavorite(Long songId) {
        if (songId == null || songId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid song ID");
        }
        AppUserEntity user = requireCurrentUser();
        SongEntity song = requireSong(songId);
        favoriteRepository.findByUserAndSong(user, song).ifPresentOrElse(
            f -> {
                favoriteRepository.delete(f);
                logger.info("Removed favorite song {} for user {}", songId, user.getUsername());
            },
            () -> logger.debug("Favorite not found for song {} and user {}", songId, user.getUsername())
        );
    }

    @Transactional
    public List<PlaylistResponse> getPlaylists() {
        AppUserEntity user = requireCurrentUser();
        List<PlaylistResponse> playlists = playlistRepository.findByUser(user).stream()
            .map(this::toPlaylistResponse)
            .toList();
        logger.debug("Retrieved {} playlists for user: {}", playlists.size(), user.getUsername());
        return playlists;
    }

    public PlaylistResponse createPlaylist(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist name must not be blank");
        }
        AppUserEntity user = requireCurrentUser();
        PlaylistEntity playlist = new PlaylistEntity();
        playlist.setName(name.trim());
        playlist.setUser(user);
        playlist = playlistRepository.save(playlist);
        logger.info("Playlist created: {} (ID: {}) for user: {}", playlist.getName(), playlist.getId(), user.getUsername());
        return toPlaylistResponse(playlist);
    }

    public PlaylistResponse addSongToPlaylist(Long playlistId, Long songId) {
        if (playlistId == null || playlistId <= 0 || songId == null || songId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid playlist or song ID");
        }
        PlaylistEntity playlist = requirePlaylist(playlistId);
        SongEntity song = requireSong(songId);
        if (!playlistSongRepository.existsByPlaylistAndSong(playlist, song)) {
            PlaylistSongEntity item = new PlaylistSongEntity();
            item.setPlaylist(playlist);
            item.setSong(song);
            playlistSongRepository.save(item);
            logger.info("Song {} added to playlist {} (ID: {})", song.getTitle(), playlist.getName(), playlistId);
        } else {
            logger.debug("Song {} already in playlist {}", song.getTitle(), playlist.getName());
        }
        return toPlaylistResponse(playlist);
    }

    public PlaylistResponse removeSongFromPlaylist(Long playlistId, Long songId) {
        if (playlistId == null || playlistId <= 0 || songId == null || songId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid playlist or song ID");
        }
        PlaylistEntity playlist = requirePlaylist(playlistId);
        SongEntity song = requireSong(songId);
        playlistSongRepository.findByPlaylistAndSong(playlist, song).ifPresentOrElse(
            item -> {
                playlistSongRepository.delete(item);
                logger.info("Song {} removed from playlist {} (ID: {})", song.getTitle(), playlist.getName(), playlistId);
            },
            () -> logger.debug("Song {} not in playlist {}", song.getTitle(), playlist.getName())
        );
        return toPlaylistResponse(playlist);
    }

    public SongResponse playSong(Long songId) {
        if (songId == null || songId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid song ID");
        }
        SongEntity song = requireSong(songId);
        song.setPlays(song.getPlays() + 1);
        songRepository.save(song);

        AppUserEntity user = requireCurrentUser();
        HistoryEntryEntity history = new HistoryEntryEntity();
        history.setUser(user);
        history.setSong(song);
        history.setPlayedAt(LocalDateTime.now());
        historyEntryRepository.save(history);

        logger.info("Song played: {} (now {} plays)", song.getTitle(), song.getPlays());
        return toSongResponse(song, currentUserOrNull());
    }

    @Transactional
    public List<HistoryResponse> getHistory() {
        AppUserEntity user = requireCurrentUser();
        List<HistoryResponse> history = historyEntryRepository.findByUserOrderByPlayedAtDesc(user).stream()
            .map(entry -> {
                SongEntity song = entry.getSong();
                ArtistEntity artist = song != null ? song.getArtist() : null;
                return new HistoryResponse(
                    entry.getId(),
                    song != null ? song.getId() : null,
                    song != null ? song.getTitle() : "Unknown",
                    artist != null ? artist.getName() : "Unknown",
                    entry.getPlayedAt().format(TIME_FORMATTER)
                );
            })
            .toList();
        logger.debug("Retrieved {} history entries for user: {}", history.size(), user.getUsername());
        return history;
    }

    public Map<String, String> clearHistory() {
        AppUserEntity user = requireCurrentUser();
        historyEntryRepository.deleteByUser(user);
        logger.info("History cleared for user: {}", user.getUsername());
        return Map.of("message", "History cleared");
    }

    @Transactional
    public List<CommentResponse> getComments(Long songId) {
        if (songId == null || songId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid song ID");
        }
        List<CommentResponse> comments = commentRepository.findBySongOrderByCreatedAtDesc(requireSong(songId)).stream()
            .map(comment -> new CommentResponse(
                comment.getId(),
                comment.getSong().getId(),
                comment.getUser() != null ? comment.getUser().getFullName() : "Unknown",
                comment.getMessage(),
                comment.getRating(),
                comment.getCreatedAt().format(TIME_FORMATTER)
            ))
            .toList();
        logger.debug("Retrieved {} comments for song: {}", comments.size(), songId);
        return comments;
    }

    public CommentResponse addComment(Long songId, CommentRequest request) {
        if (songId == null || songId <= 0 || request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        AppUserEntity user = requireCurrentUser();
        SongEntity song = requireSong(songId);

        CommentEntity comment = new CommentEntity();
        comment.setSong(song);
        comment.setUser(user);
        comment.setMessage(request.message().trim());
        comment.setRating(request.rating());
        comment.setCreatedAt(LocalDateTime.now());
        comment = commentRepository.save(comment);

        upsertRating(song, user, request.rating());

        logger.info("Comment added to song {} by user {}", song.getTitle(), user.getUsername());
        return new CommentResponse(comment.getId(), song.getId(), user.getFullName(), comment.getMessage(), comment.getRating(), comment.getCreatedAt().format(TIME_FORMATTER));
    }

    public SongResponse rateSong(Long songId, RatingRequest request) {
        if (songId == null || songId <= 0 || request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        SongEntity song = requireSong(songId);
        AppUserEntity user = requireCurrentUser();
        upsertRating(song, user, request.rating());
        logger.info("Rating {} added to song {} by user {}", request.rating(), song.getTitle(), user.getUsername());
        return toSongResponse(song, currentUserOrNull());
    }

    @Transactional
    public AdminSummaryResponse getAdminSummary() {
        requireAdmin();
        AdminSummaryResponse summary = new AdminSummaryResponse(
            (int) userRepository.count(),
            (int) songRepository.count(),
            (int) artistRepository.count(),
            (int) albumRepository.count(),
            (int) genreRepository.count()
        );
        logger.debug("Admin summary retrieved");
        return summary;
    }

    @Transactional
    public List<UserResponse> getUsers() {
        requireAdmin();
        List<UserResponse> users = userRepository.findAll().stream()
            .map(this::toUserResponse)
            .toList();
        logger.debug("Retrieved {} users", users.size());
        return users;
    }

    public UserResponse lockUser(Long userId, boolean locked) {
        requireAdmin();
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID");
        }
        AppUserEntity user = requireUser(userId);
        user.setLocked(locked);
        userRepository.save(user);
        logger.info("User {} lock status changed to: {} (ID: {})", user.getUsername(), locked, userId);
        return toUserResponse(user);
    }

    public Artist createArtist(AdminArtistRequest request) {
        requireAdmin();
        if (request == null || request.name() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        ArtistEntity entity = new ArtistEntity();
        entity.setName(request.name().trim());
        entity.setCountry(request.country().trim());
        entity.setImageUrl(request.imageUrl().trim());
        entity = artistRepository.save(entity);
        logger.info("Artist created: {} (ID: {})", entity.getName(), entity.getId());
        return new Artist(entity.getId(), entity.getName(), entity.getCountry(), entity.getImageUrl());
    }

    public Artist updateArtist(Long artistId, AdminArtistRequest request) {
        requireAdmin();
        if (artistId == null || artistId <= 0 || request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        ArtistEntity entity = requireArtist(artistId);
        entity.setName(request.name().trim());
        entity.setCountry(request.country().trim());
        entity.setImageUrl(request.imageUrl().trim());
        entity = artistRepository.save(entity);
        logger.info("Artist updated: {} (ID: {})", entity.getName(), entity.getId());
        return new Artist(entity.getId(), entity.getName(), entity.getCountry(), entity.getImageUrl());
    }

    public Map<String, String> deleteArtist(Long artistId) {
        requireAdmin();
        if (artistId == null || artistId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid artist ID");
        }
        ArtistEntity artist = requireArtist(artistId);
        if (albumRepository.countByArtist(artist) > 0 || songRepository.countByArtist(artist) > 0) {
            logger.warn("Cannot delete artist {} - still has albums or songs", artistId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Artist still has albums or songs");
        }
        artistRepository.delete(artist);
        logger.info("Artist deleted: {} (ID: {})", artist.getName(), artistId);
        return Map.of("message", "Artist deleted");
    }

    public Album createAlbum(AdminAlbumRequest request) {
        requireAdmin();
        if (request == null || request.title() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        AlbumEntity entity = new AlbumEntity();
        entity.setTitle(request.title().trim());
        entity.setArtist(requireArtist(request.artistId()));
        entity.setReleaseYear(request.releaseYear());
        entity.setCoverColor(request.coverColor().trim());
        entity = albumRepository.save(entity);
        logger.info("Album created: {} (ID: {})", entity.getTitle(), entity.getId());
        return new Album(entity.getId(), entity.getTitle(), entity.getArtist().getId(), entity.getReleaseYear(), entity.getCoverColor());
    }

    public Album updateAlbum(Long albumId, AdminAlbumRequest request) {
        requireAdmin();
        if (albumId == null || albumId <= 0 || request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        AlbumEntity entity = requireAlbum(albumId);
        entity.setTitle(request.title().trim());
        entity.setArtist(requireArtist(request.artistId()));
        entity.setReleaseYear(request.releaseYear());
        entity.setCoverColor(request.coverColor().trim());
        entity = albumRepository.save(entity);
        logger.info("Album updated: {} (ID: {})", entity.getTitle(), entity.getId());
        return new Album(entity.getId(), entity.getTitle(), entity.getArtist().getId(), entity.getReleaseYear(), entity.getCoverColor());
    }

    public Map<String, String> deleteAlbum(Long albumId) {
        requireAdmin();
        if (albumId == null || albumId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid album ID");
        }
        AlbumEntity album = requireAlbum(albumId);
        if (songRepository.countByAlbum(album) > 0) {
            logger.warn("Cannot delete album {} - still has songs", albumId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Album still has songs");
        }
        albumRepository.delete(album);
        logger.info("Album deleted: {} (ID: {})", album.getTitle(), albumId);
        return Map.of("message", "Album deleted");
    }

    public List<String> createGenre(AdminGenreRequest request) {
        requireAdmin();
        if (request == null || request.name() == null || request.name().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Genre name cannot be empty");
        }
        String genreName = request.name().trim();
        if (!genreRepository.existsByNameIgnoreCase(genreName)) {
            GenreEntity entity = new GenreEntity();
            entity.setName(genreName);
            genreRepository.save(entity);
            logger.info("Genre created: {}", genreName);
        } else {
            logger.debug("Genre already exists: {}", genreName);
        }
        return getGenres();
    }

    public List<String> deleteGenre(String genreName) {
        requireAdmin();
        if (genreName == null || genreName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Genre name cannot be empty");
        }
        GenreEntity genre = genreRepository.findByNameIgnoreCase(genreName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found"));
        if (songRepository.countByGenre(genre) > 0) {
            logger.warn("Cannot delete genre {} - still assigned to songs", genreName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Genre is still assigned to songs");
        }
        genreRepository.delete(genre);
        logger.info("Genre deleted: {}", genreName);
        return getGenres();
    }

    public SongResponse createSong(AdminSongRequest request) {
        requireAdmin();
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        SongEntity song = new SongEntity();
        applySong(song, request);
        song = songRepository.save(song);
        logger.info("Song created: {} (ID: {})", song.getTitle(), song.getId());
        return toSongResponse(song, currentUserOrNull());
    }

    public SongResponse updateSong(Long songId, AdminSongRequest request) {
        requireAdmin();
        if (songId == null || songId <= 0 || request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data");
        }
        SongEntity song = requireSong(songId);
        applySong(song, request);
        song = songRepository.save(song);
        logger.info("Song updated: {} (ID: {})", song.getTitle(), song.getId());
        return toSongResponse(song, currentUserOrNull());
    }

    public Map<String, String> deleteSong(Long songId) {
        requireAdmin();
        if (songId == null || songId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid song ID");
        }
        SongEntity song = requireSong(songId);
        favoriteRepository.deleteBySong(song);
        playlistSongRepository.deleteBySong(song);
        commentRepository.deleteBySong(song);
        songRatingRepository.deleteBySong(song);
        historyEntryRepository.deleteBySong(song);
        songRepository.delete(song);
        logger.info("Song deleted: {} (ID: {})", song.getTitle(), songId);
        return Map.of("message", "Song deleted");
    }

    private void applySong(SongEntity song, AdminSongRequest request) {
        song.setTitle(request.title().trim());
        song.setArtist(requireArtist(request.artistId()));
        song.setAlbum(requireAlbum(request.albumId()));
        song.setGenre(requireGenre(request.genre()));
        song.setDurationSeconds(request.durationSeconds());
        song.setPlays(request.plays());
        song.setTrending(request.trending());
        song.setAudioUrl(request.audioUrl().trim());
    }

    private boolean matchesSearch(SongEntity song, String searchText) {
        return String.join(" ",
                song.getTitle(),
                song.getGenre().getName(),
                song.getArtist().getName(),
                song.getAlbum().getTitle())
            .toLowerCase(Locale.ROOT)
            .contains(searchText);
    }

    private Set<Long> favoriteSongIds(AppUserEntity user) {
        return favoriteRepository.findByUser(user).stream()
            .map(favorite -> favorite.getSong().getId())
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> playlistSongIds(AppUserEntity user) {
        Set<Long> ids = new LinkedHashSet<>();
        playlistRepository.findByUser(user).forEach(playlist ->
            playlistSongRepository.findByPlaylist(playlist).forEach(item -> ids.add(item.getSong().getId()))
        );
        return ids;
    }

    private SongResponse toSongResponse(SongEntity song, AppUserEntity user) {
        List<SongRatingEntity> ratings = songRatingRepository.findBySong(song);
        double averageRating = ratings.isEmpty() ? 0.0 : ratings.stream().mapToInt(SongRatingEntity::getRating).average().orElse(0.0);
        boolean favorite = user != null && favoriteRepository.existsByUserAndSong(user, song);
        boolean inAnyPlaylist = user != null && playlistSongIds(user).contains(song.getId());

        return new SongResponse(
            song.getId(),
            song.getTitle(),
            song.getArtist().getName(),
            song.getAlbum().getTitle(),
            song.getGenre().getName(),
            song.getDurationSeconds(),
            "%02d:%02d".formatted(song.getDurationSeconds() / 60, song.getDurationSeconds() % 60),
            song.getPlays(),
            song.isTrending(),
            favorite,
            inAnyPlaylist,
            song.getAudioUrl(),
            averageRating,
            ratings.size()
        );
    }

    private PlaylistResponse toPlaylistResponse(PlaylistEntity playlist) {
        List<Long> songIds = playlistSongRepository.findByPlaylist(playlist).stream()
            .map(item -> item.getSong().getId())
            .toList();
        return new PlaylistResponse(playlist.getId(), playlist.getName(), songIds.size(), songIds);
    }

    private UserResponse toUserResponse(AppUserEntity user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRoleName(), user.isLocked(), favoriteSongIds(user));
    }

    private void upsertRating(SongEntity song, AppUserEntity user, int rating) {
        SongRatingEntity entity = songRatingRepository.findBySongAndUser(song, user).orElseGet(() -> {
            SongRatingEntity created = new SongRatingEntity();
            created.setSong(song);
            created.setUser(user);
            return created;
        });
        entity.setRating(rating);
        songRatingRepository.save(entity);
    }

    private void requireAdmin() {
        AppUserEntity user = requireCurrentUser();
        if (!"ADMIN".equals(user.getRoleName())) {
            logger.warn("Unauthorized admin access attempt by user: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }

    private AppUserEntity requireCurrentUser() {
        AppUserEntity user = currentUserOrNull();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first");
        }
        return user;
    }

    @Transactional
    private AppUserEntity currentUserOrNull() {
        try {
            return appSessionRepository.findById(CURRENT_SESSION_ID)
                .map(AppSessionEntity::getCurrentUser)
                .orElse(null);
        } catch (Exception e) {
            logger.error("Error retrieving current user from session", e);
            return null;
        }
    }

    private void setCurrentUser(AppUserEntity user) {
        try {
            AppSessionEntity session = appSessionRepository.findById(CURRENT_SESSION_ID)
                .orElseGet(() -> {
                    AppSessionEntity entity = new AppSessionEntity();
                    entity.setId(CURRENT_SESSION_ID);
                    return entity;
                });
            session.setCurrentUser(user);
            appSessionRepository.save(session);
            logger.debug("Current user set to: {}", user == null ? "null" : user.getUsername());
        } catch (Exception e) {
            logger.error("Error setting current user in session", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Session management error");
        }
    }

    private AppUserEntity requireUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ArtistEntity requireArtist(Long id) {
        return artistRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist not found"));
    }

    private AlbumEntity requireAlbum(Long id) {
        return albumRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
    }

    private GenreEntity requireGenre(String name) {
        return genreRepository.findByNameIgnoreCase(name.trim()).orElseGet(() -> {
            GenreEntity genre = new GenreEntity();
            genre.setName(name.trim());
            return genreRepository.save(genre);
        });
    }

    private SongEntity requireSong(Long id) {
        return songRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));
    }

    private PlaylistEntity requirePlaylist(Long id) {
        PlaylistEntity playlist = playlistRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        if (!playlist.getUser().getId().equals(requireCurrentUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Playlist does not belong to current user");
        }
        return playlist;
    }
}
