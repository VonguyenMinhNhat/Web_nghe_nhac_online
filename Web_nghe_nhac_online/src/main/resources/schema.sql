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
