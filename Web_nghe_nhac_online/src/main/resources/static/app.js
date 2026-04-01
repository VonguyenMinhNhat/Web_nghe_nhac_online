const state = {
  songs: [],
  playlists: [],
  genres: [],
  artists: [],
  albums: [],
  favorites: new Set(),
  currentSongId: null,
  currentFilter: "all",
  search: "",
  isPlaying: false,
  isMuted: false,
  volume: 1,
  user: null,
  history: [],
  comments: []
};

const refs = {
  tabButtons: Array.from(document.querySelectorAll(".tab-button")),
  tabPanels: Array.from(document.querySelectorAll(".tab-panel")),
  searchInput: document.getElementById("searchInput"),
  resultCount: document.getElementById("resultCount"),
  songGrid: document.getElementById("songGrid"),
  songCount: document.getElementById("songCount"),
  favoriteCount: document.getElementById("favoriteCount"),
  playlistCount: document.getElementById("playlistCount"),
  detailTitle: document.getElementById("detailTitle"),
  detailArtist: document.getElementById("detailArtist"),
  detailAlbum: document.getElementById("detailAlbum"),
  detailGenre: document.getElementById("detailGenre"),
  detailDuration: document.getElementById("detailDuration"),
  detailPlays: document.getElementById("detailPlays"),
  detailRating: document.getElementById("detailRating"),
  detailPlayBtn: document.getElementById("detailPlayBtn"),
  detailFavoriteBtn: document.getElementById("detailFavoriteBtn"),
  detailPlaylistSelect: document.getElementById("detailPlaylistSelect"),
  detailAddPlaylistBtn: document.getElementById("detailAddPlaylistBtn"),
  nowPlayingTitle: document.getElementById("nowPlayingTitle"),
  playBtn: document.getElementById("playBtn"),
  prevBtn: document.getElementById("prevBtn"),
  nextBtn: document.getElementById("nextBtn"),
  progressBar: document.getElementById("progressBar"),
  volumeBar: document.getElementById("volumeBar"),
  muteBtn: document.getElementById("muteBtn"),
  audioPlayer: document.getElementById("audioPlayer"),
  commentInput: document.getElementById("commentInput"),
  ratingInput: document.getElementById("ratingInput"),
  submitCommentBtn: document.getElementById("submitCommentBtn"),
  submitRatingBtn: document.getElementById("submitRatingBtn"),
  commentList: document.getElementById("commentList"),
  historyList: document.getElementById("historyList"),
  clearHistoryBtn: document.getElementById("clearHistoryBtn"),
  playlistInput: document.getElementById("playlistInput"),
  createPlaylistBtn: document.getElementById("createPlaylistBtn"),
  playlistList: document.getElementById("playlist-list"),
  authUsername: document.getElementById("authUsername"),
  authFullName: document.getElementById("authFullName"),
  authEmail: document.getElementById("authEmail"),
  authPassword: document.getElementById("authPassword"),
  registerBtn: document.getElementById("registerBtn"),
  loginBtn: document.getElementById("loginBtn"),
  logoutBtn: document.getElementById("logoutBtn"),
  forgotEmail: document.getElementById("forgotEmail"),
  forgotBtn: document.getElementById("forgotBtn"),
  resetCode: document.getElementById("resetCode"),
  resetPassword: document.getElementById("resetPassword"),
  resetBtn: document.getElementById("resetBtn"),
  profileUsername: document.getElementById("profileUsername"),
  profileFullName: document.getElementById("profileFullName"),
  profileEmail: document.getElementById("profileEmail"),
  updateProfileBtn: document.getElementById("updateProfileBtn"),
  currentPassword: document.getElementById("currentPassword"),
  newPassword: document.getElementById("newPassword"),
  changePasswordBtn: document.getElementById("changePasswordBtn"),
  adminGenreName: document.getElementById("adminGenreName"),
  createGenreBtn: document.getElementById("createGenreBtn"),
  adminGenreList: document.getElementById("adminGenreList"),
  adminArtistName: document.getElementById("adminArtistName"),
  adminArtistCountry: document.getElementById("adminArtistCountry"),
  createArtistBtn: document.getElementById("createArtistBtn"),
  adminArtistList: document.getElementById("adminArtistList"),
  adminAlbumTitle: document.getElementById("adminAlbumTitle"),
  adminAlbumArtistSelect: document.getElementById("adminAlbumArtistSelect"),
  createAlbumBtn: document.getElementById("createAlbumBtn"),
  adminAlbumList: document.getElementById("adminAlbumList"),
  adminSongTitle: document.getElementById("adminSongTitle"),
  adminSongArtistSelect: document.getElementById("adminSongArtistSelect"),
  adminSongAlbumSelect: document.getElementById("adminSongAlbumSelect"),
  adminSongGenreSelect: document.getElementById("adminSongGenreSelect"),
  createSongBtn: document.getElementById("createSongBtn"),
  adminSongList: document.getElementById("adminSongList"),
  adminTotalUsers: document.getElementById("adminTotalUsers"),
  adminTotalSongs: document.getElementById("adminTotalSongs"),
  adminTotalArtists: document.getElementById("adminTotalArtists"),
  adminTotalAlbums: document.getElementById("adminTotalAlbums"),
  adminTotalGenres: document.getElementById("adminTotalGenres")
};

const pendingSearch = { timer: null };

async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json" };
  const response = await fetch(path, {
    headers,
    credentials: "same-origin",
    ...options
  });

  if (!response.ok) {
    if (response.status === 204) {
      return null;
    }
    const payload = await response.text();
    throw new Error(payload || `API request failed: ${response.status}`);
  }

  if (response.status === 204 || response.headers.get("Content-Length") === "0") {
    return null;
  }

  return response.json();
}

function safeText(text) {
  return text == null ? "-" : String(text);
}

function setActiveTab(targetId) {
  refs.tabButtons.forEach((button) => {
    button.classList.toggle("active", button.dataset.target === targetId);
  });
  refs.tabPanels.forEach((panel) => {
    panel.classList.toggle("active", panel.id === targetId);
  });
}

function getCurrentSong() {
  return state.songs.find((song) => song.id === state.currentSongId) || state.songs[0] || null;
}

function renderSongCount() {
  const count = state.songs.length;
  refs.songCount.textContent = String(count);
  refs.playlistCount.textContent = String(state.playlists.length);
  refs.favoriteCount.textContent = String(state.favorites.size);
}

function formatSeconds(seconds) {
  if (!Number.isFinite(seconds)) {
    return "00:00";
  }
  const minutes = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${String(minutes).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
}

function renderSongGrid() {
  const filtered = state.songs.filter((song) => {
    const query = state.search.trim().toLowerCase();
    if (query && !`${song.title} ${song.artist} ${song.album}`.toLowerCase().includes(query)) {
      return false;
    }
    if (state.currentFilter === "trending") {
      return song.trending;
    }
    if (state.currentFilter === "favorites") {
      return state.favorites.has(song.id);
    }
    if (state.currentFilter === "playlist") {
      return state.playlists.length > 0 && state.playlists[0].songIds?.includes(song.id);
    }
    return true;
  });

  refs.resultCount.textContent = `${filtered.length} ket qua`;
  refs.songGrid.innerHTML = filtered.map((song) => {
    const active = state.currentSongId === song.id ? "active" : "false";
    return `
      <article class="song-card" data-song-id="${song.id}" aria-pressed="${active}">
        <h3>${song.title}</h3>
        <p>${song.artist} · ${song.album}</p>
        <p>${formatSeconds(song.durationSeconds)} • ${song.plays.toLocaleString()}</p>
        <button type="button" data-action="play">Phat</button>
      </article>`;
  }).join("");

  Array.from(refs.songGrid.querySelectorAll(".song-card")).forEach((card) => {
    const songId = Number(card.dataset.songId);
    const button = card.querySelector("button[data-action='play']");
    button?.addEventListener("click", () => selectSong(songId));
  });
}

function renderDetailPanel(song) {
  if (!song) {
    refs.detailTitle.textContent = "Chon mot bai hat de xem chi tiet";
    refs.detailArtist.textContent = "-";
    refs.detailAlbum.textContent = "-";
    refs.detailGenre.textContent = "-";
    refs.detailDuration.textContent = "00:00";
    refs.detailPlays.textContent = "0";
    refs.detailRating.textContent = "0";
    return;
  }
  refs.detailTitle.textContent = song.title;
  refs.detailArtist.textContent = song.artist;
  refs.detailAlbum.textContent = song.album;
  refs.detailGenre.textContent = song.genre;
  refs.detailDuration.textContent = formatSeconds(song.durationSeconds);
  refs.detailPlays.textContent = String(song.plays);
  refs.detailRating.textContent = String(song.rating ?? 0);
}

function renderNowPlaying(song) {
  refs.nowPlayingTitle.textContent = song ? song.title : "Chua co bai hat";
  refs.playBtn.textContent = state.isPlaying ? "Dung" : "Phat";
  refs.volumeBar.value = String(state.volume);
  refs.muteBtn.textContent = state.isMuted ? "Mo tieng" : "Tat tieng";
}

function renderPlaylists() {
  refs.playlistCount.textContent = String(state.playlists.length);
  refs.detailPlaylistSelect.innerHTML = state.playlists.map((playlist) => `
    <option value="${playlist.id}">${playlist.name}</option>
  `).join("");
}

function renderAdminLists() {
  refs.adminGenreList.innerHTML = state.genres.map((genre) => `<li>${genre}</li>`).join("");
  refs.adminArtistList.innerHTML = state.artists.map((artist) => `<li>${artist.name}</li>`).join("");
  refs.adminAlbumList.innerHTML = state.albums.map((album) => `<li>${album.title}</li>`).join("");
  refs.adminSongList.innerHTML = state.songs.map((song) => `<li>${song.title}</li>`).join("");
  refs.adminAlbumArtistSelect.innerHTML = state.artists.map((artist) => `<option value="${artist.id}">${artist.name}</option>`).join("");
  refs.adminSongArtistSelect.innerHTML = refs.adminAlbumArtistSelect.innerHTML;
  refs.adminSongAlbumSelect.innerHTML = state.albums.map((album) => `<option value="${album.id}">${album.title}</option>`).join("");
  refs.adminSongGenreSelect.innerHTML = state.genres.map((genre) => `<option value="${genre}">${genre}</option>`).join("");
}

function renderAdminSummary(summary) {
  if (!summary) {
    return;
  }
  refs.adminTotalUsers.textContent = String(summary.totalUsers);
  refs.adminTotalSongs.textContent = String(summary.totalSongs);
  refs.adminTotalArtists.textContent = String(summary.totalArtists);
  refs.adminTotalAlbums.textContent = String(summary.totalAlbums);
  refs.adminTotalGenres.textContent = String(summary.totalGenres);
}

async function selectSong(songId) {
  const song = state.songs.find((item) => item.id === songId);
  if (!song) {
    return;
  }
  state.currentSongId = songId;
  state.isPlaying = true;
  refs.audioPlayer.src = song.audioUrl;
  refs.audioPlayer.currentTime = 0;
  try {
    await refs.audioPlayer.play();
  } catch {
    // ignore autoplay restrictions
  }
  if (!state.history.some((entry) => entry.id === song.id)) {
    state.history.unshift({ id: song.id, title: song.title, artist: song.artist });
    if (state.history.length > 12) {
      state.history.pop();
    }
  }
  renderDetailPanel(song);
  renderNowPlaying(song);
  renderHistory();
}

function togglePlay() {
  if (!state.currentSongId) {
    const first = state.songs[0];
    if (first) {
      selectSong(first.id);
    }
    return;
  }
  state.isPlaying = !state.isPlaying;
  if (state.isPlaying) {
    refs.audioPlayer.play().catch(() => {});
  } else {
    refs.audioPlayer.pause();
  }
  renderNowPlaying(getCurrentSong());
}

function nextSong() {
  const currentIndex = state.songs.findIndex((song) => song.id === state.currentSongId);
  const nextIndex = currentIndex < state.songs.length - 1 ? currentIndex + 1 : 0;
  selectSong(state.songs[nextIndex]?.id);
}

function prevSong() {
  const currentIndex = state.songs.findIndex((song) => song.id === state.currentSongId);
  const prevIndex = currentIndex > 0 ? currentIndex - 1 : state.songs.length - 1;
  selectSong(state.songs[prevIndex]?.id);
}

function renderHistory() {
  refs.historyList.innerHTML = state.history.map((entry) => `<li>${entry.title} — ${entry.artist}</li>`).join("");
}

function updateFilterButtons(filter) {
  state.currentFilter = filter;
  document.querySelectorAll(".filter-button").forEach((button) => {
    button.classList.toggle("active", button.dataset.filter === filter);
  });
  renderSongGrid();
}

async function handleSearch(event) {
  state.search = event.target.value || "";
  clearTimeout(pendingSearch.timer);
  pendingSearch.timer = setTimeout(() => renderSongGrid(), 120);
}

async function submitComment() {
  const song = getCurrentSong();
  if (!song) return;
  const message = refs.commentInput.value.trim();
  const rating = Number(refs.ratingInput.value) || 1;
  if (!message) return;
  try {
    await api(`/api/songs/${song.id}/comments`, {
      method: "POST",
      body: JSON.stringify({ message, rating })
    });
    refs.commentList.insertAdjacentHTML("afterbegin", `<li>${message} — ${rating} sao</li>`);
    refs.commentInput.value = "";
  } catch (error) {
    console.warn(error);
  }
}

async function submitRating() {
  const song = getCurrentSong();
  if (!song) return;
  const rating = Number(refs.ratingInput.value) || 1;
  try {
    const updated = await api(`/api/songs/${song.id}/rating`, {
      method: "POST",
      body: JSON.stringify({ rating })
    });
    if (updated && updated.totalRatings != null) {
      refs.detailRating.textContent = String(updated.totalRatings);
    }
  } catch (error) {
    console.warn(error);
  }
}

async function createPlaylist() {
  const name = (refs.playlistInput?.value || "").trim();
  if (!name) return;
  try {
    const playlist = await api("/api/playlists", {
      method: "POST",
      body: JSON.stringify({ name })
    });
    if (playlist?.id) {
      state.playlists.push(playlist);
      refs.playlistInput.value = "";
      renderPlaylists();
    }
  } catch {
    const fallback = { id: Date.now(), name, songIds: [] };
    state.playlists.push(fallback);
    refs.playlistInput.value = "";
    renderPlaylists();
  }
}

function renderPlaylists() {
  refs.playlistCount.textContent = String(state.playlists.length);
  refs.detailPlaylistSelect.innerHTML = state.playlists.map((playlist) => `
    <option value="${playlist.id}">${playlist.name}</option>
  `).join("");
  refs.playlistList && (refs.playlistList.innerHTML = state.playlists.map((playlist) => `<li>${playlist.name}</li>`).join(""));
}

async function addCurrentSongToPlaylist() {
  const song = getCurrentSong();
  const playlistId = Number(refs.detailPlaylistSelect.value);
  if (!song || !playlistId) return;
  const playlist = state.playlists.find((value) => value.id === playlistId);
  if (!playlist) return;
  try {
    const updated = await api(`/api/playlists/${playlistId}/songs/${song.id}`, { method: "POST" });
    if (updated?.songIds) {
      playlist.songIds = updated.songIds;
    }
  } catch {
    playlist.songIds = Array.from(new Set([...(playlist.songIds || []), song.id]));
  }
}

function toggleFavoriteSong() {
  const song = getCurrentSong();
  if (!song) return;
  if (state.favorites.has(song.id)) {
    state.favorites.delete(song.id);
  } else {
    state.favorites.add(song.id);
  }
  refs.favoriteCount.textContent = String(state.favorites.size);
  renderSongGrid();
}

async function clearHistory() {
  try {
    await api("/api/history", { method: "DELETE" });
  } catch {
    // ignore
  }
  state.history = [];
  renderHistory();
}

async function registerUser() {
  const username = (refs.authUsername.value || "").trim();
  const fullName = (refs.authFullName.value || "").trim();
  const email = (refs.authEmail.value || "").trim();
  const password = refs.authPassword.value || "";
  if (!username || !email || !password) return;
  try {
    await api("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ username, fullName, email, password })
    });
    refs.authUsername.value = "";
    refs.authFullName.value = "";
    refs.authEmail.value = "";
    refs.authPassword.value = "";
  } catch (error) {
    console.warn(error);
  }
}

async function loginUser() {
  const email = (refs.authEmail.value || "").trim();
  const password = refs.authPassword.value || "";
  if (!email || !password) return;
  try {
    const user = await api("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    });
    if (user?.user) {
      state.user = user.user;
      await loadUser();
    }
  } catch (error) {
    console.warn(error);
  }
}

async function logoutUser() {
  try {
    await api("/api/auth/logout", { method: "POST" });
  } catch {
    // ignore
  }
  state.user = null;
  refs.profileUsername.value = "";
  refs.profileFullName.value = "";
  refs.profileEmail.value = "";
}

async function forgotPassword() {
  const email = (refs.forgotEmail.value || "").trim();
  if (!email) return;
  try {
    await api("/api/auth/forgot-password", {
      method: "POST",
      body: JSON.stringify({ email })
    });
    refs.forgotEmail.value = "";
  } catch (error) {
    console.warn(error);
  }
}

async function resetPassword() {
  const email = (refs.authEmail.value || "").trim();
  const code = (refs.resetCode.value || "").trim();
  const password = refs.resetPassword.value || "";
  if (!email || !code || !password) return;
  try {
    await api("/api/auth/reset-password", {
      method: "POST",
      body: JSON.stringify({ email, resetCode: code, newPassword: password })
    });
    refs.resetCode.value = "";
    refs.resetPassword.value = "";
  } catch (error) {
    console.warn(error);
  }
}

async function updateProfile() {
  const username = (refs.profileUsername.value || "").trim();
  const fullName = (refs.profileFullName.value || "").trim();
  const email = (refs.profileEmail.value || "").trim();
  if (!username || !email) return;
  try {
    const updated = await api("/api/users/me", {
      method: "PUT",
      body: JSON.stringify({ username, fullName, email })
    });
    if (updated) {
      state.user = updated;
    }
  } catch (error) {
    console.warn(error);
  }
}

async function changePassword() {
  const currentPassword = refs.currentPassword.value || "";
  const newPassword = refs.newPassword.value || "";
  if (!currentPassword || !newPassword) return;
  try {
    await api("/api/users/me/change-password", {
      method: "POST",
      body: JSON.stringify({ currentPassword, newPassword })
    });
    refs.currentPassword.value = "";
    refs.newPassword.value = "";
  } catch (error) {
    console.warn(error);
  }
}

async function createGenre() {
  const name = (refs.adminGenreName.value || "").trim();
  if (!name) return;
  try {
    const genres = await api("/api/admin/genres", {
      method: "POST",
      body: JSON.stringify({ name })
    });
    if (Array.isArray(genres)) {
      state.genres = genres;
      refs.adminGenreName.value = "";
      renderAdminLists();
    }
  } catch (error) {
    console.warn(error);
  }
}

async function createArtist() {
  const name = (refs.adminArtistName.value || "").trim();
  const country = (refs.adminArtistCountry.value || "").trim();
  if (!name || !country) return;
  try {
    const artist = await api("/api/admin/artists", {
      method: "POST",
      body: JSON.stringify({ name, country, imageUrl: "" })
    });
    if (artist?.id) {
      state.artists.push(artist);
      refs.adminArtistName.value = "";
      refs.adminArtistCountry.value = "";
      renderAdminLists();
    }
  } catch (error) {
    console.warn(error);
  }
}

async function createAlbum() {
  const title = (refs.adminAlbumTitle.value || "").trim();
  const artistId = Number(refs.adminAlbumArtistSelect.value);
  if (!title || !artistId) return;
  try {
    const album = await api("/api/admin/albums", {
      method: "POST",
      body: JSON.stringify({ title, artistId, releaseYear: new Date().getFullYear(), coverColor: "#000000" })
    });
    if (album?.id) {
      state.albums.push(album);
      refs.adminAlbumTitle.value = "";
      renderAdminLists();
    }
  } catch (error) {
    console.warn(error);
  }
}

async function createSong() {
  const title = (refs.adminSongTitle.value || "").trim();
  const artistId = Number(refs.adminSongArtistSelect.value);
  const albumId = Number(refs.adminSongAlbumSelect.value);
  const genre = refs.adminSongGenreSelect.value || "Pop";
  if (!title || !artistId || !albumId || !genre) return;
  try {
    const song = await api("/api/admin/songs", {
      method: "POST",
      body: JSON.stringify({
        title,
        artistId,
        albumId,
        genre,
        durationSeconds: 180,
        plays: 0,
        trending: false,
        audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
      })
    });
    if (song?.id) {
      state.songs.push(song);
      refs.adminSongTitle.value = "";
      renderSongGrid();
      renderAdminLists();
      renderSongCount();
    }
  } catch (error) {
    console.warn(error);
  }
}

async function loadUser() {
  try {
    const user = await api("/api/users/me");
    state.user = user;
    if (user) {
      refs.profileUsername.value = user.username || "";
      refs.profileFullName.value = user.fullName || "";
      refs.profileEmail.value = user.email || "";
    }
  } catch {
    state.user = null;
  }
}

async function loadDashboard() {
  try {
    const dashboard = await api("/api/dashboard");
    if (dashboard) {
      refs.songCount.textContent = String(dashboard.totalSongs ?? state.songs.length);
      refs.favoriteCount.textContent = String(dashboard.favoriteCount ?? state.favorites.size);
      refs.playlistCount.textContent = String(dashboard.playlistCount ?? state.playlists.length);
      renderAdminSummary(dashboard.adminSummary);
    }
  } catch {
    // ignore
  }
}

async function loadSongs() {
  try {
    const songs = await api("/api/songs");
    if (Array.isArray(songs)) {
      state.songs = songs;
      renderSongCount();
      renderSongGrid();
    }
  } catch {
    state.songs = [];
  }
}

async function loadGenres() {
  try {
    const genres = await api("/api/genres");
    if (Array.isArray(genres)) {
      state.genres = genres;
    }
  } catch {
    state.genres = [];
  }
}

async function loadArtists() {
  try {
    const artists = await api("/api/artists");
    if (Array.isArray(artists)) {
      state.artists = artists;
    }
  } catch {
    state.artists = [];
  }
}

async function loadAlbums() {
  try {
    const albums = await api("/api/albums");
    if (Array.isArray(albums)) {
      state.albums = albums;
    }
  } catch {
    state.albums = [];
  }
}

async function loadPlaylists() {
  try {
    const playlists = await api("/api/playlists");
    if (Array.isArray(playlists)) {
      state.playlists = playlists;
      renderPlaylists();
    }
  } catch {
    state.playlists = [];
  }
}

async function loadFavorites() {
  try {
    const favorites = await api("/api/favorites");
    if (Array.isArray(favorites) || favorites instanceof Set) {
      state.favorites = new Set(Array.from(favorites));
    }
  } catch {
    state.favorites = new Set();
  }
}

async function loadAdminSummary() {
  try {
    const summary = await api("/api/admin/summary");
    renderAdminSummary(summary);
  } catch {
    // ignore
  }
}

function attachEventListeners() {
  refs.tabButtons.forEach((button) => {
    button.addEventListener("click", () => setActiveTab(button.dataset.target));
  });
  refs.searchInput?.addEventListener("input", handleSearch);
  document.querySelectorAll(".filter-button").forEach((button) => {
    button.addEventListener("click", () => updateFilterButtons(button.dataset.filter));
  });
  refs.detailPlayBtn?.addEventListener("click", togglePlay);
  refs.playBtn?.addEventListener("click", togglePlay);
  refs.prevBtn?.addEventListener("click", prevSong);
  refs.nextBtn?.addEventListener("click", nextSong);
  refs.volumeBar?.addEventListener("input", (event) => {
    const value = Number(event.target.value);
    state.volume = Number.isFinite(value) ? value : 1;
    refs.audioPlayer.volume = state.volume;
  });
  refs.muteBtn?.addEventListener("click", () => {
    state.isMuted = !state.isMuted;
    refs.audioPlayer.muted = state.isMuted;
    renderNowPlaying(getCurrentSong());
  });
  refs.submitCommentBtn?.addEventListener("click", submitComment);
  refs.submitRatingBtn?.addEventListener("click", submitRating);
  refs.clearHistoryBtn?.addEventListener("click", clearHistory);
  refs.createPlaylistBtn?.addEventListener("click", createPlaylist);
  refs.detailAddPlaylistBtn?.addEventListener("click", addCurrentSongToPlaylist);
  refs.detailFavoriteBtn?.addEventListener("click", toggleFavoriteSong);
  refs.registerBtn?.addEventListener("click", registerUser);
  refs.loginBtn?.addEventListener("click", loginUser);
  refs.logoutBtn?.addEventListener("click", logoutUser);
  refs.forgotBtn?.addEventListener("click", forgotPassword);
  refs.resetBtn?.addEventListener("click", resetPassword);
  refs.updateProfileBtn?.addEventListener("click", updateProfile);
  refs.changePasswordBtn?.addEventListener("click", changePassword);
  refs.createGenreBtn?.addEventListener("click", createGenre);
  refs.createArtistBtn?.addEventListener("click", createArtist);
  refs.createAlbumBtn?.addEventListener("click", createAlbum);
  refs.createSongBtn?.addEventListener("click", createSong);
  refs.audioPlayer?.addEventListener("ended", () => {
    state.isPlaying = false;
    renderNowPlaying(getCurrentSong());
  });
}

async function initializeApp() {
  attachEventListeners();
  await Promise.all([
    loadUser(),
    loadGenres(),
    loadArtists(),
    loadAlbums(),
    loadPlaylists(),
    loadFavorites()
  ]);
  await Promise.all([loadSongs(), loadDashboard(), loadAdminSummary()]);
  renderPlaylists();
  renderAdminLists();
  renderSongGrid();
  renderDetailPanel(getCurrentSong());
  renderNowPlaying(getCurrentSong());
}

window.addEventListener("DOMContentLoaded", initializeApp);
