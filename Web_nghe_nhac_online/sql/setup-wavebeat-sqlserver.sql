IF DB_ID(N'WaveBeatDB') IS NULL
BEGIN
    CREATE DATABASE WaveBeatDB;
END
GO

USE WaveBeatDB;
GO

IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        full_name NVARCHAR(120) NOT NULL,
        email NVARCHAR(120) NOT NULL UNIQUE,
        password NVARCHAR(120) NOT NULL,
        role_name NVARCHAR(30) NOT NULL,
        locked BIT NOT NULL DEFAULT 0
    )
END
GO

IF OBJECT_ID(N'dbo.artists', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.artists (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(120) NOT NULL,
        country NVARCHAR(60) NOT NULL,
        image_url NVARCHAR(255) NOT NULL
    )
END
GO

IF OBJECT_ID(N'dbo.genres', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.genres (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(80) NOT NULL UNIQUE
    )
END
GO

IF OBJECT_ID(N'dbo.albums', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.albums (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title NVARCHAR(150) NOT NULL,
        artist_id BIGINT NOT NULL,
        release_year INT NOT NULL,
        cover_color NVARCHAR(30) NOT NULL,
        CONSTRAINT FK_albums_artist FOREIGN KEY (artist_id) REFERENCES dbo.artists(id)
    )
END
GO

IF OBJECT_ID(N'dbo.songs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.songs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title NVARCHAR(150) NOT NULL,
        artist_id BIGINT NOT NULL,
        album_id BIGINT NOT NULL,
        genre_id BIGINT NOT NULL,
        duration_seconds INT NOT NULL,
        plays BIGINT NOT NULL,
        trending BIT NOT NULL DEFAULT 0,
        audio_url NVARCHAR(500) NOT NULL,
        CONSTRAINT FK_songs_artist FOREIGN KEY (artist_id) REFERENCES dbo.artists(id),
        CONSTRAINT FK_songs_album FOREIGN KEY (album_id) REFERENCES dbo.albums(id),
        CONSTRAINT FK_songs_genre FOREIGN KEY (genre_id) REFERENCES dbo.genres(id)
    )
END
GO

IF OBJECT_ID(N'dbo.favorites', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.favorites (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        song_id BIGINT NOT NULL,
        CONSTRAINT UQ_favorites UNIQUE(user_id, song_id),
        CONSTRAINT FK_favorites_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
        CONSTRAINT FK_favorites_song FOREIGN KEY (song_id) REFERENCES dbo.songs(id)
    )
END
GO

IF OBJECT_ID(N'dbo.playlists', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.playlists (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        name NVARCHAR(120) NOT NULL,
        CONSTRAINT FK_playlists_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    )
END
GO

IF OBJECT_ID(N'dbo.playlist_songs', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.playlist_songs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        playlist_id BIGINT NOT NULL,
        song_id BIGINT NOT NULL,
        CONSTRAINT UQ_playlist_song UNIQUE(playlist_id, song_id),
        CONSTRAINT FK_playlist_songs_playlist FOREIGN KEY (playlist_id) REFERENCES dbo.playlists(id),
        CONSTRAINT FK_playlist_songs_song FOREIGN KEY (song_id) REFERENCES dbo.songs(id)
    )
END
GO

IF OBJECT_ID(N'dbo.comments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.comments (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        song_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        message NVARCHAR(1000) NOT NULL,
        rating INT NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_comments_song FOREIGN KEY (song_id) REFERENCES dbo.songs(id),
        CONSTRAINT FK_comments_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    )
END
GO

IF OBJECT_ID(N'dbo.song_ratings', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.song_ratings (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        song_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        rating INT NOT NULL,
        CONSTRAINT UQ_song_ratings UNIQUE(song_id, user_id),
        CONSTRAINT FK_song_ratings_song FOREIGN KEY (song_id) REFERENCES dbo.songs(id),
        CONSTRAINT FK_song_ratings_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    )
END
GO

IF OBJECT_ID(N'dbo.history_entries', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.history_entries (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        song_id BIGINT NOT NULL,
        played_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_history_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
        CONSTRAINT FK_history_song FOREIGN KEY (song_id) REFERENCES dbo.songs(id)
    )
END
GO

IF OBJECT_ID(N'dbo.reset_tokens', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.reset_tokens (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        email NVARCHAR(120) NOT NULL,
        reset_code NVARCHAR(80) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    )
END
GO

IF OBJECT_ID(N'dbo.app_sessions', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.app_sessions (
        id BIGINT PRIMARY KEY,
        current_user_id BIGINT NULL,
        CONSTRAINT FK_app_sessions_user FOREIGN KEY (current_user_id) REFERENCES dbo.users(id)
    )
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.users ON
    INSERT INTO dbo.users (id, username, full_name, email, password, role_name, locked) VALUES
    (1, 'admin', 'Administrator', 'demo@wavebeat.local', 'demo123', 'ADMIN', 0),
    (2, 'listener', 'Listener Sample', 'listener@wavebeat.local', 'listener123', 'USER', 0)
    SET IDENTITY_INSERT dbo.users OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.artists WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.artists ON
    INSERT INTO dbo.artists (id, name, country, image_url) VALUES
    (1, 'Luna Grey', 'UK', 'sunset'),
    (2, 'Neon Rivers', 'US', 'neon'),
    (3, 'Blue Horizon', 'VN', 'ocean'),
    (4, 'Nova Pulse', 'JP', 'pulse'),
    (5, 'Ha Linh', 'VN', 'street'),
    (6, 'The Sundrops', 'AU', 'gold')
    SET IDENTITY_INSERT dbo.artists OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.genres WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.genres ON
    INSERT INTO dbo.genres (id, name) VALUES
    (1, 'Pop'),
    (2, 'R&B'),
    (3, 'Chill'),
    (4, 'EDM'),
    (5, 'Acoustic'),
    (6, 'Indie'),
    (7, 'Synthwave'),
    (8, 'Lo-fi')
    SET IDENTITY_INSERT dbo.genres OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.albums WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.albums ON
    INSERT INTO dbo.albums (id, title, artist_id, release_year, cover_color) VALUES
    (1, 'Afterglow', 1, 2024, '#ffb677'),
    (2, 'Neon Nights', 2, 2023, '#84d7ff'),
    (3, 'Shoreline', 3, 2025, '#69d2a6'),
    (4, 'Firewire', 4, 2025, '#ff8a3d'),
    (5, 'Pho Sau Mua', 5, 2024, '#c5a46d'),
    (6, 'First Light', 6, 2022, '#ffe8a3')
    SET IDENTITY_INSERT dbo.albums OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.songs WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.songs ON
    INSERT INTO dbo.songs (id, title, artist_id, album_id, genre_id, duration_seconds, plays, trending, audio_url) VALUES
    (1, 'City Lights', 1, 1, 1, 372, 12458, 1, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3'),
    (2, 'Midnight Flow', 2, 2, 2, 331, 9844, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3'),
    (3, 'Ocean Echo', 3, 3, 3, 303, 15670, 1, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3'),
    (4, 'Fireline', 4, 4, 4, 288, 17602, 1, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3'),
    (5, 'Mua Qua Hiem Pho', 5, 5, 5, 252, 7320, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3'),
    (6, 'Golden Morning', 6, 6, 6, 322, 8642, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3'),
    (7, 'Static Hearts', 2, 2, 7, 295, 14015, 1, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3'),
    (8, 'Cloud Terrace', 3, 3, 8, 267, 6201, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3')
    SET IDENTITY_INSERT dbo.songs OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.favorites WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.favorites ON
    INSERT INTO dbo.favorites (id, user_id, song_id) VALUES
    (1, 1, 1), (2, 1, 3), (3, 1, 7)
    SET IDENTITY_INSERT dbo.favorites OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.playlists WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.playlists ON
    INSERT INTO dbo.playlists (id, user_id, name) VALUES
    (1, 1, 'Morning Drive'),
    (2, 1, 'Late Night')
    SET IDENTITY_INSERT dbo.playlists OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.playlist_songs WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.playlist_songs ON
    INSERT INTO dbo.playlist_songs (id, playlist_id, song_id) VALUES
    (1, 1, 1), (2, 1, 4), (3, 1, 6), (4, 2, 2), (5, 2, 3)
    SET IDENTITY_INSERT dbo.playlist_songs OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.comments WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.comments ON
    INSERT INTO dbo.comments (id, song_id, user_id, message, rating, created_at) VALUES
    (1, 1, 1, N'Bai nay rat bat tai va de nho.', 5, SYSUTCDATETIME()),
    (2, 3, 2, N'Khong gian am nhac nhe va de tap trung.', 4, SYSUTCDATETIME())
    SET IDENTITY_INSERT dbo.comments OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.song_ratings WHERE id = 1)
BEGIN
    SET IDENTITY_INSERT dbo.song_ratings ON
    INSERT INTO dbo.song_ratings (id, song_id, user_id, rating) VALUES
    (1, 1, 1, 5),
    (2, 3, 2, 4)
    SET IDENTITY_INSERT dbo.song_ratings OFF
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.app_sessions WHERE id = 1)
BEGIN
    INSERT INTO dbo.app_sessions (id, current_user_id) VALUES (1, 1)
END
GO

SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = N'dbo'
ORDER BY TABLE_NAME;
GO
