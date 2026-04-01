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

IF NOT EXISTS (SELECT 1 FROM dbo.songs WHERE id = 9)
BEGIN
    SET IDENTITY_INSERT dbo.songs ON
    INSERT INTO dbo.songs (id, title, artist_id, album_id, genre_id, duration_seconds, plays, trending, audio_url) VALUES
    (9, 'Sunset Drive', 1, 1, 1, 356, 11457, 1, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3'),
    (10, 'Neon Dreams', 2, 2, 2, 308, 10234, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3'),
    (11, 'Sapphire Tide', 3, 3, 3, 341, 8741, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3'),
    (12, 'Voltage Pulse', 4, 4, 4, 309, 10620, 1, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3'),
    (13, 'Saigon Nights', 5, 5, 5, 276, 9412, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3'),
    (14, 'Dawn Parade', 6, 6, 6, 315, 6538, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3'),
    (15, 'Neon Skyline', 2, 2, 7, 298, 13201, 1, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3'),
    (16, 'Cloudburst', 3, 3, 8, 263, 5842, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3'),
    (17, 'Afterparty', 1, 1, 1, 325, 13284, 1, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-17.mp3'),
    (18, 'Moonlit Stroll', 5, 5, 5, 289, 7420, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-18.mp3'),
    (19, 'Electro Pulse', 4, 4, 4, 330, 10981, 1, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-19.mp3'),
    (20, 'Daydream', 6, 6, 3, 270, 6912, 0, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-20.mp3')
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
