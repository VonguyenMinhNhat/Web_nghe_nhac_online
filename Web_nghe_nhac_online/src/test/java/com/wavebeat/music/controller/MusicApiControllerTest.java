package com.wavebeat.music.controller;

import com.wavebeat.music.dto.AdminSummaryResponse;
import com.wavebeat.music.dto.AuthResponse;
import com.wavebeat.music.dto.CommentResponse;
import com.wavebeat.music.dto.DashboardResponse;
import com.wavebeat.music.dto.PlaylistResponse;
import com.wavebeat.music.dto.SongResponse;
import com.wavebeat.music.dto.UserResponse;
import com.wavebeat.music.model.Album;
import com.wavebeat.music.model.Artist;
import com.wavebeat.music.service.MusicLibraryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(MusicApiController.class)
class MusicApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MusicLibraryService musicLibraryService;

    @Test
    void shouldRegisterLoginAndResetPassword() throws Exception {
        UserResponse registeredUser = new UserResponse(3L, "newuser", "New User", "new@wavebeat.local", "USER", false, Set.of());
        when(musicLibraryService.register(any())).thenReturn(new AuthResponse("Registered successfully", registeredUser));
        when(musicLibraryService.logout()).thenReturn(new AuthResponse("Logout successful", null));
        when(musicLibraryService.login(any())).thenReturn(new AuthResponse("Login successful", registeredUser));
        when(musicLibraryService.forgotPassword(any())).thenReturn(Map.of("message", "Reset code generated", "resetCode", "RESET-3"));
        when(musicLibraryService.resetPassword(any())).thenReturn(new AuthResponse("Password reset successful", registeredUser));

        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("""
                    {
                      "username": "newuser",
                      "fullName": "New User",
                      "email": "new@wavebeat.local",
                      "password": "pass123"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user.email").value("new@wavebeat.local"));

        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                    {
                      "email": "new@wavebeat.local",
                      "password": "pass123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Login successful"));

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType("application/json")
                .content("""
                    {
                      "email": "new@wavebeat.local"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resetCode").value("RESET-3"));

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType("application/json")
                .content("""
                    {
                      "email": "new@wavebeat.local",
                      "resetCode": "RESET-3",
                      "newPassword": "pass456"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password reset successful"));
    }

    @Test
    void shouldUpdateProfileAndChangePassword() throws Exception {
        when(musicLibraryService.updateProfile(any())).thenReturn(
            new UserResponse(1L, "admin_updated", "Administrator Updated", "demo@wavebeat.local", "ADMIN", false, Set.of(1L, 3L))
        );
        when(musicLibraryService.changePassword(any())).thenReturn(
            new AuthResponse("Password changed", new UserResponse(1L, "admin_updated", "Administrator Updated", "demo@wavebeat.local", "ADMIN", false, Set.of(1L, 3L)))
        );

        mockMvc.perform(put("/api/users/me")
                .contentType("application/json")
                .content("""
                    {
                      "username": "admin_updated",
                      "fullName": "Administrator Updated",
                      "email": "demo@wavebeat.local"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin_updated"));

        mockMvc.perform(post("/api/users/me/change-password")
                .contentType("application/json")
                .content("""
                    {
                      "currentPassword": "demo123",
                      "newPassword": "demo456"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password changed"));
    }

    @Test
    void shouldManageFavoritesPlaylistsHistoryCommentsAndRating() throws Exception {
        doNothing().when(musicLibraryService).addFavorite(2L);
        when(musicLibraryService.getFavorites()).thenReturn(Set.of(1L, 2L, 3L));
        when(musicLibraryService.createPlaylist(anyString())).thenReturn(new PlaylistResponse(3L, "Functional Playlist", 0, List.of()));
        when(musicLibraryService.addSongToPlaylist(3L, 2L)).thenReturn(new PlaylistResponse(3L, "Functional Playlist", 1, List.of(2L)));
        when(musicLibraryService.playSong(2L)).thenReturn(sampleSong(2L, "Midnight Flow"));
        when(musicLibraryService.getHistory()).thenReturn(List.of(
            new com.wavebeat.music.dto.HistoryResponse(1L, 2L, "Midnight Flow", "Neon Rivers", "2026-04-01 10:00:00")
        ));
        when(musicLibraryService.addComment(anyLong(), any())).thenReturn(
            new CommentResponse(1L, 2L, "Administrator", "Bai nay cuon hut", 5, "2026-04-01 10:05:00")
        );
        when(musicLibraryService.rateSong(anyLong(), any())).thenReturn(
            new SongResponse(2L, "Midnight Flow", "Neon Rivers", "Neon Nights", "R&B", 331, "05:31", 9844, false, false, false, "https://example.com/song.mp3", 4.0, 1)
        );

        mockMvc.perform(post("/api/favorites/2"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favorites"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@ == 2)]").exists());

        mockMvc.perform(post("/api/playlists")
                .contentType("application/json")
                .content("""
                    {
                      "name": "Functional Playlist"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Functional Playlist"));

        mockMvc.perform(post("/api/playlists/3/songs/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.songIds[0]").value(2));

        mockMvc.perform(post("/api/songs/2/play"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Midnight Flow"));

        mockMvc.perform(get("/api/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].songTitle").value("Midnight Flow"));

        mockMvc.perform(post("/api/songs/2/comments")
                .contentType("application/json")
                .content("""
                    {
                      "message": "Bai nay cuon hut",
                      "rating": 5
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(post("/api/songs/2/rating")
                .contentType("application/json")
                .content("""
                    {
                      "rating": 4
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRatings").value(1));
    }

    @Test
    void shouldExecuteAdminCrudFlows() throws Exception {
        when(musicLibraryService.createGenre(any())).thenReturn(List.of("Chill", "Jazz", "Pop"));
        when(musicLibraryService.createArtist(any())).thenReturn(new Artist(7L, "Admin Artist", "VN", "img"));
        when(musicLibraryService.createAlbum(any())).thenReturn(new Album(7L, "Admin Album", 7L, 2026, "#ffffff"));
        when(musicLibraryService.createSong(any())).thenReturn(
            new SongResponse(9L, "Admin Song", "Admin Artist", "Admin Album", "Jazz", 180, "03:00", 0, false, false, false, "https://example.com/song.mp3", 0.0, 0)
        );
        when(musicLibraryService.lockUser(anyLong(), anyBoolean())).thenReturn(
            new UserResponse(2L, "listener", "Listener Sample", "listener@wavebeat.local", "USER", true, Set.of())
        );
        when(musicLibraryService.deleteSong(9L)).thenReturn(Map.of("message", "Song deleted"));

        mockMvc.perform(post("/api/admin/genres")
                .contentType("application/json")
                .content("""
                    {
                      "name": "Jazz"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[?(@ == 'Jazz')]").exists());

        mockMvc.perform(post("/api/admin/artists")
                .contentType("application/json")
                .content("""
                    {
                      "name": "Admin Artist",
                      "country": "VN",
                      "imageUrl": "img"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(7));

        mockMvc.perform(post("/api/admin/albums")
                .contentType("application/json")
                .content("""
                    {
                      "title": "Admin Album",
                      "artistId": 7,
                      "releaseYear": 2026,
                      "coverColor": "#ffffff"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(7));

        mockMvc.perform(post("/api/admin/songs")
                .contentType("application/json")
                .content("""
                    {
                      "title": "Admin Song",
                      "artistId": 7,
                      "albumId": 7,
                      "genre": "Jazz",
                      "durationSeconds": 180,
                      "plays": 0,
                      "trending": false,
                      "audioUrl": "https://example.com/song.mp3"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Admin Song"));

        mockMvc.perform(patch("/api/admin/users/2/lock")
                .contentType("application/json")
                .content("""
                    {
                      "locked": true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.locked").value(true));

        mockMvc.perform(delete("/api/admin/songs/9"))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldExposeReferenceEndpoints() throws Exception {
        when(musicLibraryService.getDashboard()).thenReturn(
            new DashboardResponse(8, 3, 5, List.of("Pop", "R&B"), sampleSong(1L, "City Lights"))
        );
        when(musicLibraryService.getSongs(anyString(), anyString(), anyString())).thenReturn(List.of(sampleSong(1L, "City Lights")));
        when(musicLibraryService.getGenres()).thenReturn(List.of("Pop", "R&B"));
        when(musicLibraryService.getArtists()).thenReturn(List.of(new Artist(1L, "Luna Grey", "UK", "sunset")));
        when(musicLibraryService.getAlbums()).thenReturn(List.of(new Album(1L, "Afterglow", 1L, 2024, "#ffb677")));
        when(musicLibraryService.getCurrentUser()).thenReturn(new UserResponse(1L, "admin", "Administrator", "demo@wavebeat.local", "ADMIN", false, Set.of(1L)));
        when(musicLibraryService.getAdminSummary()).thenReturn(new AdminSummaryResponse(2, 8, 6, 6, 8));

        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.heroSong.title").value("City Lights"));

        mockMvc.perform(get("/api/songs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("City Lights"));

        mockMvc.perform(get("/api/genres"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("Pop"));

        mockMvc.perform(get("/api/artists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Luna Grey"));

        mockMvc.perform(get("/api/albums"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Afterglow"));

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("demo@wavebeat.local"));

        mockMvc.perform(get("/api/admin/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalSongs").value(8));
    }

    private SongResponse sampleSong(Long id, String title) {
        return new SongResponse(
            id,
            title,
            "Neon Rivers",
            "Neon Nights",
            "R&B",
            331,
            "05:31",
            9844,
            false,
            false,
            false,
            "https://example.com/song.mp3",
            4.0,
            1
        );
    }
}
