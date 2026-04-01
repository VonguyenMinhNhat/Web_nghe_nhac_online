package com.wavebeat.music.controller;

import com.wavebeat.music.dto.AdminAlbumRequest;
import com.wavebeat.music.dto.AdminArtistRequest;
import com.wavebeat.music.dto.AdminGenreRequest;
import com.wavebeat.music.dto.AdminSongRequest;
import com.wavebeat.music.dto.AdminSummaryResponse;
import com.wavebeat.music.dto.AuthResponse;
import com.wavebeat.music.dto.ChangePasswordRequest;
import com.wavebeat.music.dto.CommentRequest;
import com.wavebeat.music.dto.CommentResponse;
import com.wavebeat.music.dto.CreatePlaylistRequest;
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
import com.wavebeat.music.dto.UserLockRequest;
import com.wavebeat.music.dto.UserResponse;
import com.wavebeat.music.model.Album;
import com.wavebeat.music.model.Artist;
import com.wavebeat.music.service.MusicLibraryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class MusicApiController {

    private final MusicLibraryService musicLibraryService;

    public MusicApiController(MusicLibraryService musicLibraryService) {
        this.musicLibraryService = musicLibraryService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "wavebeat-api");
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return musicLibraryService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return musicLibraryService.login(request);
    }

    @PostMapping("/auth/logout")
    public AuthResponse logout() {
        return musicLibraryService.logout();
    }

    @PostMapping("/auth/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return musicLibraryService.forgotPassword(request);
    }

    @PostMapping("/auth/reset-password")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return musicLibraryService.resetPassword(request);
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return musicLibraryService.getDashboard();
    }

    @GetMapping("/songs")
    public List<SongResponse> songs(
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "all") String genre,
        @RequestParam(defaultValue = "all") String filter
    ) {
        return musicLibraryService.getSongs(search, genre, filter);
    }

    @GetMapping("/songs/{songId}")
    public SongResponse song(@PathVariable @Positive Long songId) {
        return musicLibraryService.getSong(songId);
    }

    @PostMapping("/songs/{songId}/play")
    public SongResponse playSong(@PathVariable @Positive Long songId) {
        return musicLibraryService.playSong(songId);
    }

    @GetMapping("/songs/{songId}/comments")
    public List<CommentResponse> comments(@PathVariable @Positive Long songId) {
        return musicLibraryService.getComments(songId);
    }

    @PostMapping("/songs/{songId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable @Positive Long songId, @Valid @RequestBody CommentRequest request) {
        return musicLibraryService.addComment(songId, request);
    }

    @PostMapping("/songs/{songId}/rating")
    public SongResponse rateSong(@PathVariable @Positive Long songId, @Valid @RequestBody RatingRequest request) {
        return musicLibraryService.rateSong(songId, request);
    }

    @GetMapping("/genres")
    public List<String> genres() {
        return musicLibraryService.getGenres();
    }

    @GetMapping("/artists")
    public List<Artist> artists() {
        return musicLibraryService.getArtists();
    }

    @GetMapping("/albums")
    public List<Album> albums() {
        return musicLibraryService.getAlbums();
    }

    @GetMapping("/users/me")
    public UserResponse me() {
        return musicLibraryService.getCurrentUser();
    }

    @PutMapping("/users/me")
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return musicLibraryService.updateProfile(request);
    }

    @PostMapping("/users/me/change-password")
    public AuthResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return musicLibraryService.changePassword(request);
    }

    @GetMapping("/favorites")
    public Set<Long> favorites() {
        return musicLibraryService.getFavorites();
    }

    @PostMapping("/favorites/{songId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFavorite(@PathVariable @Positive Long songId) {
        musicLibraryService.addFavorite(songId);
    }

    @DeleteMapping("/favorites/{songId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(@PathVariable @Positive Long songId) {
        musicLibraryService.removeFavorite(songId);
    }

    @GetMapping("/playlists")
    public List<PlaylistResponse> playlists() {
        return musicLibraryService.getPlaylists();
    }

    @PostMapping("/playlists")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistResponse createPlaylist(@Valid @RequestBody CreatePlaylistRequest request) {
        return musicLibraryService.createPlaylist(request.name());
    }

    @PostMapping("/playlists/{playlistId}/songs/{songId}")
    public PlaylistResponse addSongToPlaylist(@PathVariable @Positive Long playlistId, @PathVariable @Positive Long songId) {
        return musicLibraryService.addSongToPlaylist(playlistId, songId);
    }

    @DeleteMapping("/playlists/{playlistId}/songs/{songId}")
    public PlaylistResponse removeSongFromPlaylist(@PathVariable @Positive Long playlistId, @PathVariable @Positive Long songId) {
        return musicLibraryService.removeSongFromPlaylist(playlistId, songId);
    }

    @GetMapping("/history")
    public List<HistoryResponse> history() {
        return musicLibraryService.getHistory();
    }

    @DeleteMapping("/history")
    public Map<String, String> clearHistory() {
        return musicLibraryService.clearHistory();
    }

    @GetMapping("/admin/summary")
    public AdminSummaryResponse adminSummary() {
        return musicLibraryService.getAdminSummary();
    }

    @GetMapping("/admin/users")
    public List<UserResponse> users() {
        return musicLibraryService.getUsers();
    }

    @PatchMapping("/admin/users/{userId}/lock")
    public UserResponse lockUser(@PathVariable @Positive Long userId, @Valid @RequestBody UserLockRequest request) {
        return musicLibraryService.lockUser(userId, request.locked());
    }

    @PostMapping("/admin/artists")
    @ResponseStatus(HttpStatus.CREATED)
    public Artist createArtist(@Valid @RequestBody AdminArtistRequest request) {
        return musicLibraryService.createArtist(request);
    }

    @PutMapping("/admin/artists/{artistId}")
    public Artist updateArtist(@PathVariable @Positive Long artistId, @Valid @RequestBody AdminArtistRequest request) {
        return musicLibraryService.updateArtist(artistId, request);
    }

    @DeleteMapping("/admin/artists/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArtist(@PathVariable @Positive Long artistId) {
        musicLibraryService.deleteArtist(artistId);
    }

    @PostMapping("/admin/albums")
    @ResponseStatus(HttpStatus.CREATED)
    public Album createAlbum(@Valid @RequestBody AdminAlbumRequest request) {
        return musicLibraryService.createAlbum(request);
    }

    @PutMapping("/admin/albums/{albumId}")
    public Album updateAlbum(@PathVariable @Positive Long albumId, @Valid @RequestBody AdminAlbumRequest request) {
        return musicLibraryService.updateAlbum(albumId, request);
    }

    @DeleteMapping("/admin/albums/{albumId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlbum(@PathVariable @Positive Long albumId) {
        musicLibraryService.deleteAlbum(albumId);
    }

    @PostMapping("/admin/genres")
    @ResponseStatus(HttpStatus.CREATED)
    public List<String> createGenre(@Valid @RequestBody AdminGenreRequest request) {
        return musicLibraryService.createGenre(request);
    }

    @DeleteMapping("/admin/genres/{genreName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGenre(@PathVariable @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Genre name must contain only letters and spaces") String genreName) {
        musicLibraryService.deleteGenre(genreName);
    }

    @PostMapping("/admin/songs")
    @ResponseStatus(HttpStatus.CREATED)
    public SongResponse createSong(@Valid @RequestBody AdminSongRequest request) {
        return musicLibraryService.createSong(request);
    }

    @PutMapping("/admin/songs/{songId}")
    public SongResponse updateSong(@PathVariable @Positive Long songId, @Valid @RequestBody AdminSongRequest request) {
        return musicLibraryService.updateSong(songId, request);
    }

    @DeleteMapping("/admin/songs/{songId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSong(@PathVariable @Positive Long songId) {
        musicLibraryService.deleteSong(songId);
    }
}
