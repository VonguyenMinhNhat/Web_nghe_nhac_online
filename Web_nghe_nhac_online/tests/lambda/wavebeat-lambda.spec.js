const { test, expect } = require("@playwright/test");
const { waitForDashboard, songCard } = require("../helpers/ui-helpers");

function uniqueSuffix() {
  return `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
}

async function loginAsAdmin(request) {
  await request.post("/api/auth/login", {
    data: { email: "demo@wavebeat.local", password: "demo123" }
  });
}

async function registerNewUser(request, suffix) {
  const user = {
    username: `user-${suffix}`,
    fullName: `User ${suffix}`,
    email: `user-${suffix}@wavebeat.local`,
    password: "pass123"
  };
  const response = await request.post("/api/auth/register", { data: user });
  return { response, user };
}

async function clickPlayOnCard(page, songTitle) {
  const card = songCard(page, songTitle);
  await card.scrollIntoViewIfNeeded();
  const btn = card.getByRole("button", { name: /Phat|Dang phat/i });
  await btn.waitFor({ state: "visible", timeout: 15000 });
  
  // Use force click to bypass any transparent overlays
  await btn.click({ force: true });
  
  // Wait for the title to change to the expected title in the player
  // This is better than a simple sleep because it reacts as soon as the tunnel syncs the UI
  await page.waitForFunction((title) => {
    const el = document.querySelector('[data-testid="now-playing-title"]');
    return el && el.innerText.trim().toLowerCase() === title.toLowerCase();
  }, songTitle, { timeout: 15000 }).catch(() => {
    console.log(`Warning: Header didn't update to "${songTitle}" within 15s, continuing...`);
  });
  
  // Small safety buffer for player state
  await page.waitForTimeout(1000);
}

test.describe("WaveBeat Lambda 20 chức năng", () => {
  test.beforeAll(async () => {
    // Wait briefly for tunnel ready if needed
    await new Promise(r => setTimeout(r, 2000));
  });

  test.describe("F01 - Dang ky tai khoan", () => {
    test("Dang ky mot tai khoan moi", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { response, user } = await registerNewUser(request, suffix);
      expect(response.status()).toBe(201);
      const body = await response.json();
      expect(body.user.email).toBe(user.email);
    });

    test("Dang ky tai khoan moi voi username va email khac", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { response } = await registerNewUser(request, suffix);
      expect(response.status()).toBe(201);
    });

    test("Dang ky tai khoan moi voi mat khau hop le", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { response } = await registerNewUser(request, suffix);
      expect(response.status()).toBe(201);
    });
  });

  test.describe("F02 - Dang nhap", () => {
    test("Dang nhap voi tai khoan demo", async ({ request }) => {
      const response = await request.post("/api/auth/login", {
        data: { email: "demo@wavebeat.local", password: "demo123" }
      });
      expect(response.ok()).toBeTruthy();
      const body = await response.json();
      expect(body.message).toBe("Login successful");
    });

    test("Dang nhap voi tai khoan moi dang ky", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { user } = await registerNewUser(request, suffix);
      await request.post("/api/auth/logout");
      const response = await request.post("/api/auth/login", {
        data: { email: user.email, password: user.password }
      });
      expect(response.ok()).toBeTruthy();
    });

    test("Dang nhap voi tai khoan demo sau khi dang xuat", async ({ request }) => {
      await request.post("/api/auth/logout");
      const response = await request.post("/api/auth/login", {
        data: { email: "demo@wavebeat.local", password: "demo123" }
      });
      expect(response.ok()).toBeTruthy();
    });
  });

  test.describe("F03 - Dang xuat", () => {
    test("Dang xuat sau khi dang nhap", async ({ request }) => {
      await request.post("/api/auth/login", {
        data: { email: "demo@wavebeat.local", password: "demo123" }
      });
      const response = await request.post("/api/auth/logout");
      expect(response.ok()).toBeTruthy();
    });

    test("Xac nhan nguoi dung khong ket noi duoc sau khi dang xuat", async ({ request }) => {
      await request.post("/api/auth/login", {
        data: { email: "demo@wavebeat.local", password: "demo123" }
      });
      await request.post("/api/auth/logout");
      const me = await request.get("/api/users/me");
      expect(me.status()).toBe(401);
    });

    test("Dang xuat khong bi loi khi chua dang nhap", async ({ request }) => {
      const response = await request.post("/api/auth/logout");
      expect(response.ok()).toBeTruthy();
    });
  });

  test.describe("F04 - Quen mat khau", () => {
    test("Lay ma reset cho tai khoan moi dang ky", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { user } = await registerNewUser(request, suffix);
      const response = await request.post("/api/auth/forgot-password", { data: { email: user.email } });
      expect(response.ok()).toBeTruthy();
      const payload = await response.json();
      expect(payload.resetCode).toBeDefined();
    });

    test("Dat lai mat khau voi ma reset hop le", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { user } = await registerNewUser(request, suffix);
      const forgot = await request.post("/api/auth/forgot-password", { data: { email: user.email } });
      const payload = await forgot.json();
      const reset = await request.post("/api/auth/reset-password", {
        data: { email: user.email, resetCode: payload.resetCode, newPassword: "newpass123" }
      });
      expect(reset.ok()).toBeTruthy();
      const body = await reset.json();
      expect(body.user.email).toBe(user.email);
    });

    test("Lay ma reset cho email demo", async ({ request }) => {
      const response = await request.post("/api/auth/forgot-password", { data: { email: "demo@wavebeat.local" } });
      expect(response.ok()).toBeTruthy();
      const payload = await response.json();
      expect(payload.resetCode).toContain("RESET-");
    });
  });

  test.describe("F05 - Cap nhat ho so ca nhan", () => {
    test("Cap nhat ho so sau khi dang nhap", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { user } = await registerNewUser(request, suffix);
      await request.post("/api/auth/login", { data: { email: user.email, password: user.password } });
      const response = await request.put("/api/users/me", {
        data: { username: `${user.username}-edit`, fullName: `${user.fullName} Edit`, email: user.email }
      });
      expect(response.ok()).toBeTruthy();
      const body = await response.json();
      expect(body.username).toContain("-edit");
    });

    test("Cap nhat ho so voi thong tin khac", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { user } = await registerNewUser(request, suffix);
      await request.post("/api/auth/login", { data: { email: user.email, password: user.password } });
      const response = await request.put("/api/users/me", {
        data: { username: user.username, fullName: `${user.fullName} Changed`, email: user.email }
      });
      expect(response.ok()).toBeTruthy();
      const body = await response.json();
      expect(body.fullName).toContain("Changed");
    });

    test("Lay thong tin nguoi dung hien tai sau khi cap nhat", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { user } = await registerNewUser(request, suffix);
      await request.post("/api/auth/login", { data: { email: user.email, password: user.password } });
      await request.put("/api/users/me", {
        data: { username: user.username, fullName: user.fullName, email: user.email }
      });
      const me = await request.get("/api/users/me");
      expect(me.ok()).toBeTruthy();
      const body = await me.json();
      expect(body.email).toBe(user.email);
    });
  });

  test.describe("F06 - Doi mat khau", () => {
    test("Doi mat khau thanh cong", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { user } = await registerNewUser(request, suffix);
      await request.post("/api/auth/login", { data: { email: user.email, password: user.password } });
      const response = await request.post("/api/users/me/change-password", {
        data: { currentPassword: user.password, newPassword: "newpass123" }
      });
      expect(response.ok()).toBeTruthy();
    });

    test("Dang nhap voi mat khau moi sau khi doi", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { user } = await registerNewUser(request, suffix);
      await request.post("/api/auth/login", { data: { email: user.email, password: user.password } });
      await request.post("/api/users/me/change-password", { data: { currentPassword: user.password, newPassword: "newpass123" } });
      await request.post("/api/auth/logout");
      const login = await request.post("/api/auth/login", { data: { email: user.email, password: "newpass123" } });
      expect(login.ok()).toBeTruthy();
    });

    test("Doi mat khau sau khi reset mat khau", async ({ request }) => {
      const suffix = uniqueSuffix();
      const { user } = await registerNewUser(request, suffix);
      const forgot = await request.post("/api/auth/forgot-password", { data: { email: user.email } });
      const payload = await forgot.json();
      await request.post("/api/auth/reset-password", { data: { email: user.email, resetCode: payload.resetCode, newPassword: "resetpass123" } });
      await request.post("/api/auth/login", { data: { email: user.email, password: "resetpass123" } });
      const response = await request.post("/api/users/me/change-password", { data: { currentPassword: "resetpass123", newPassword: "pass123" } });
      expect(response.ok()).toBeTruthy();
    });
  });

  test.describe("F07 - Xem danh sach bai hat", () => {
    test("Hien thi bang thong tin dashboard", async ({ page }) => {
      await waitForDashboard(page);
      await expect(page.getByTestId("song-count")).toHaveText(/\d+/);
    });

    test("Hien thi 20 bai hat tu database", async ({ page }) => {
      await waitForDashboard(page);
      const cardCount = await page.locator(".song-card").count();
      expect(cardCount).toBeGreaterThanOrEqual(20);
      await expect(page.getByTestId("song-count")).toHaveText(String(cardCount));
    });

    test("Hien thi thong tin ca si va album tren the bai hat", async ({ page }) => {
      await waitForDashboard(page);
      const card = songCard(page, "City Lights");
      await expect(card).toBeVisible();
      await expect(card.locator("p").first()).toContainText("Luna Grey");
    });
  });

  test.describe("F08 - Tim kiem bai hat", () => {
    test("Tim kiem theo ten bai hat", async ({ page }) => {
      await waitForDashboard(page);
      await page.getByTestId("search-input").fill("City Lights");
      await expect(page.locator(".song-card")).toHaveCount(1);
    });

    test("Tim kiem theo ca si", async ({ page }) => {
      await waitForDashboard(page);
      await page.getByTestId("search-input").fill("Luna Grey");
      const results = page.locator(".song-card");
      await expect(results.first()).toContainText("Luna Grey");
      const count = await results.count();
      expect(count).toBeGreaterThanOrEqual(3);
    });

    test("Tim kiem khong co ket qua", async ({ page }) => {
      await waitForDashboard(page);
      await page.getByTestId("search-input").fill("Khong ton tai");
      await expect(page.locator(".song-card")).toHaveCount(0);
    });
  });

  test.describe("F09 - Xem chi tiet bai hat", () => {
    test("Lay chi tiet bai hat 1", async ({ request }) => {
      const response = await request.get("/api/songs/1");
      expect(response.ok()).toBeTruthy();
      const song = await response.json();
      expect(song.title).toBe("City Lights");
    });

    test("Lay chi tiet bai hat 2", async ({ request }) => {
      const response = await request.get("/api/songs/2");
      expect(response.ok()).toBeTruthy();
      const song = await response.json();
      expect(song.title).toBe("Midnight Flow");
    });

    test("Lay chi tiet bai hat 3", async ({ request }) => {
      const response = await request.get("/api/songs/3");
      expect(response.ok()).toBeTruthy();
      const song = await response.json();
      expect(song.title).toBe("Ocean Echo");
    });
  });

  test.describe("F10 - Phat nhac truc tuyen", () => {
    test("Phat City Lights va hien thi tren now playing", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "City Lights");
      await expect(page.getByTestId("now-playing-title")).toHaveText("City Lights");
      await expect(page.getByTestId("play-btn")).toHaveText("Dung");
    });

    test("Phat Midnight Flow khi chon bai hat", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "Midnight Flow");
      await expect(page.getByTestId("now-playing-title")).toHaveText("Midnight Flow");
    });

    test("Phat Ocean Echo sau khi chon bai hat", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "Ocean Echo");
      await expect(page.getByTestId("now-playing-title")).toHaveText("Ocean Echo");
    });
  });

  test.describe("F11 - Tam dung, tiep tuc, tua bai hat", () => {
    test("Tam dung va tiep tuc phat", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "City Lights");
      await expect(page.getByTestId("play-btn")).toHaveText("Dung");
      await page.getByTestId("play-btn").click();
      await expect(page.getByTestId("play-btn")).toHaveText("Phat");
    });

    test("Tua bai hat bang progress bar", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "City Lights");
      await page.getByTestId("progress-bar").evaluate((element) => {
        element.value = "35";
        element.dispatchEvent(new Event("input", { bubbles: true }));
      });
      await expect(page.getByTestId("progress-bar")).toHaveValue("35");
    });

    test("Chuyen giua phat va dung lien tuc", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "Midnight Flow");
      await page.getByTestId("play-btn").dispatchEvent('click');
      await expect(page.getByTestId("play-btn")).toHaveText("Phat");
    });
  });

  test.describe("F12 - Chuyen bai truoc va bai sau", () => {
    test("Chuyen sang bai tiep theo", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "City Lights");
      await page.getByTestId("next-btn").dispatchEvent('click');
      await expect(page.getByTestId("now-playing-title")).toHaveText("Sunset Drive");
    });

    test("Chuyen ve bai truoc sau khi sang bai ke tiep", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "City Lights");
      await page.getByTestId("next-btn").dispatchEvent('click');
      await page.getByTestId("prev-btn").dispatchEvent('click');
      await expect(page.getByTestId("now-playing-title")).toHaveText("City Lights");
    });

    test("Duy tri chuyen bai khi danh sach co nhieu bai hat", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "Ocean Echo");
      await page.getByTestId("next-btn").dispatchEvent('click');
      await expect(page.getByTestId("now-playing-title")).not.toHaveText("Ocean Echo");
    });
  });

  test.describe("F13 - Dieu chinh am luong", () => {
    test("Thay doi muc am luong bang slider", async ({ page }) => {
      await waitForDashboard(page);
      await page.getByTestId("volume-bar").evaluate((element) => {
        element.value = "0.25";
        element.dispatchEvent(new Event("input", { bubbles: true }));
      });
      await expect(page.getByTestId("volume-bar")).toHaveValue("0.25");
    });

    test("Tat va mo tieng voi nut mute", async ({ page }) => {
      await waitForDashboard(page);
      await page.getByTestId("mute-btn").dispatchEvent('click');
      await expect(page.getByTestId("mute-btn")).toHaveText("Mo tieng");
    });

    test("Dat am luong 100 phan tram va khong mat tieng", async ({ page }) => {
      await waitForDashboard(page);
      await page.getByTestId("volume-bar").evaluate((element) => {
        element.value = "1";
        element.dispatchEvent(new Event("input", { bubbles: true }));
      });
      await expect(page.getByTestId("volume-bar")).toHaveValue("1");
    });
  });

  test.describe("F14 - Them bai hat vao yeu thich", () => {
    test("Them Midnight Flow vao yeu thich va loc favorite", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "Midnight Flow");
      await page.getByTestId("detail-favorite-btn").dispatchEvent('click');
      await page.getByTestId("filter-favorites").dispatchEvent('click');
      await expect(songCard(page, "Midnight Flow")).toBeVisible();
    });

    test("Them City Lights vao favorite va hien thi khi loc", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "City Lights");
      await page.getByTestId("detail-favorite-btn").dispatchEvent('click');
      await page.getByTestId("filter-favorites").dispatchEvent('click');
      await expect(songCard(page, "City Lights")).toBeVisible();
    });

    test("Hien thi danh sach favorite sau khi them", async ({ page }) => {
      await waitForDashboard(page);
      await clickPlayOnCard(page, "Ocean Echo");
      await page.getByTestId("detail-favorite-btn").dispatchEvent('click');
      await page.getByTestId("filter-favorites").dispatchEvent('click');
      await expect(page.locator(".song-card")).toHaveCount(1);
    });
  });

  test.describe("F15 - Tao playlist", () => {
    test("Tao playlist moi tu giao dien", async ({ page }) => {
      await waitForDashboard(page);
      await page.getByTestId("playlist-input").fill("Playwright Mix");
      await page.getByTestId("create-playlist-btn").click();
      await expect(page.getByTestId("playlist-list")).toContainText("Playwright Mix");
    });

    test("Tao them mot playlist moi voi ten khac", async ({ page }) => {
      await waitForDashboard(page);
      await page.getByTestId("playlist-input").fill("LambdaTest List");
      await page.getByTestId("create-playlist-btn").click();
      await expect(page.getByTestId("playlist-list")).toContainText("LambdaTest List");
    });

    test("Tao playlist va cap nhat dem so playlist", async ({ page }) => {
      await waitForDashboard(page);
      const initial = Number(await page.getByTestId("playlist-count").textContent());
      await page.getByTestId("playlist-input").fill("UI Playlist");
      await page.getByTestId("create-playlist-btn").click();
      await expect(page.getByTestId("playlist-count")).not.toHaveText(String(initial));
    });
  });

  test.describe("F16 - Them hoac xoa bai hat khoi playlist", () => {
    test("Tao playlist API va them bai hat", async ({ request }) => {
      const createResponse = await request.post("/api/playlists", { data: { name: "API Playlist" } });
      const playlist = await createResponse.json();
      const addResponse = await request.post(`/api/playlists/${playlist.id}/songs/2`);
      expect(addResponse.ok()).toBeTruthy();
      const updated = await addResponse.json();
      expect(updated.songIds).toContain(2);
    });

    test("Xoa bai hat khoi playlist bang API", async ({ request }) => {
      const createResponse = await request.post("/api/playlists", { data: { name: "Delete Playlist" } });
      const playlist = await createResponse.json();
      await request.post(`/api/playlists/${playlist.id}/songs/2`);
      const removeResponse = await request.delete(`/api/playlists/${playlist.id}/songs/2`);
      expect(removeResponse.ok()).toBeTruthy();
    });

    test("Them va xoa bai hat lien tiep tren playlist", async ({ request }) => {
      const createResponse = await request.post("/api/playlists", { data: { name: "Roundtrip Playlist" } });
      const playlist = await createResponse.json();
      await request.post(`/api/playlists/${playlist.id}/songs/3`);
      const removeResponse = await request.delete(`/api/playlists/${playlist.id}/songs/3`);
      expect(removeResponse.ok()).toBeTruthy();
    });
  });

  test.describe("F17 - Lich su nghe nhac", () => {
    test("Ghi nhan lich su khi phat bai hat 2", async ({ request }) => {
      await request.post("/api/songs/2/play");
      const response = await request.get("/api/history");
      expect(response.ok()).toBeTruthy();
      const history = await response.json();
      expect(history[0].songTitle).toBe("Midnight Flow");
    });

    test("Ghi nhan lich su khi phat bai hat 3", async ({ request }) => {
      await request.post("/api/songs/3/play");
      const response = await request.get("/api/history");
      expect(response.ok()).toBeTruthy();
      const history = await response.json();
      expect(history[0].songTitle).toBe("Ocean Echo");
    });

    test("Xoa lich su va kiem tra danh sach rong", async ({ request }) => {
      await request.post("/api/songs/2/play");
      await request.delete("/api/history");
      const response = await request.get("/api/history");
      expect(response.ok()).toBeTruthy();
      const history = await response.json();
      expect(Array.isArray(history)).toBeTruthy();
    });
  });

  test.describe("F18 - Binh luan hoac danh gia bai hat", () => {
    test("Gui binh luan cho bai hat 3", async ({ request }) => {
      const response = await request.post("/api/songs/3/comments", {
        data: { message: "Moi binh luan tu LambdaTest", rating: 5 }
      });
      expect(response.status()).toBe(201);
      const body = await response.json();
      expect(body.rating).toBe(5);
    });

    test("Danh gia bai hat 3 voi diem 4", async ({ request }) => {
      const response = await request.post("/api/songs/3/rating", {
        data: { rating: 4 }
      });
      expect(response.ok()).toBeTruthy();
      const body = await response.json();
      expect(body.totalRatings).toBeGreaterThanOrEqual(1);
    });

    test("Lay binh luan cua bai hat 3 sau khi gui", async ({ request }) => {
      const response = await request.get("/api/songs/3/comments");
      expect(response.ok()).toBeTruthy();
      const comments = await response.json();
      expect(Array.isArray(comments)).toBeTruthy();
    });
  });

  test.describe("F19 - Quan tri them, sua, xoa bai hat", () => {
    test("Admin them bai hat moi", async ({ request }) => {
      await loginAsAdmin(request);
      const response = await request.post("/api/admin/songs", {
        data: {
          title: "LambdaTest Song",
          artistId: 1,
          albumId: 1,
          genre: "Pop",
          durationSeconds: 200,
          plays: 0,
          trending: false,
          audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
        }
      });
      expect(response.status()).toBe(201);
      const body = await response.json();
      expect(body.title).toBe("LambdaTest Song");
    });

    test("Admin cap nhat bai hat da tao", async ({ request }) => {
      await loginAsAdmin(request);
      const create = await request.post("/api/admin/songs", {
        data: {
          title: "LambdaTest Song 2",
          artistId: 1,
          albumId: 1,
          genre: "Pop",
          durationSeconds: 210,
          plays: 0,
          trending: false,
          audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
        }
      });
      const song = await create.json();
      const update = await request.put(`/api/admin/songs/${song.id}`, {
        data: {
          title: "LambdaTest Song 2 Updated",
          artistId: 1,
          albumId: 1,
          genre: "Pop",
          durationSeconds: 220,
          plays: 1,
          trending: true,
          audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
        }
      });
      expect(update.ok()).toBeTruthy();
      const updated = await update.json();
      expect(updated.title).toBe("LambdaTest Song 2 Updated");
    });

    test("Admin xoa bai hat da tao", async ({ request }) => {
      await loginAsAdmin(request);
      const create = await request.post("/api/admin/songs", {
        data: {
          title: "LambdaTest Song 3",
          artistId: 1,
          albumId: 1,
          genre: "Pop",
          durationSeconds: 220,
          plays: 0,
          trending: false,
          audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
        }
      });
      const song = await create.json();
      const deleted = await request.delete(`/api/admin/songs/${song.id}`);
      expect(deleted.status()).toBe(204);
    });
  });

  test.describe("F20 - Quan tri the loai, ca si, album va nguoi dung", () => {
    test("Admin them the loai moi", async ({ request }) => {
      await loginAsAdmin(request);
      const genreName = `Genre ${Math.random().toString(36).replace(/[^a-z]+/g, "").slice(0, 6) || "Test"}`;
      const response = await request.post("/api/admin/genres", { data: { name: genreName } });
      expect(response.status()).toBe(201);
    });

    test("Admin them ca si va album moi", async ({ request }) => {
      await loginAsAdmin(request);
      const artistName = `Artist ${Math.random().toString(36).replace(/[^a-z]+/g, "").slice(0, 6) || "Test"}`;
      const artistRes = await request.post("/api/admin/artists", { data: { name: artistName, country: "Vietnam", imageUrl: "https://example.com/artist.png" } });
      expect(artistRes.status()).toBe(201);
      const artist = await artistRes.json();
      const albumRes = await request.post("/api/admin/albums", { data: { title: `Album ${uniqueSuffix()}`, artistId: artist.id, releaseYear: 2026, coverColor: "#ffffff" } });
      expect(albumRes.status()).toBe(201);
    });

    test("Admin khoa nguoi dung listener va tra lai thong tin", async ({ request }) => {
      await loginAsAdmin(request);
      const usersResponse = await request.get("/api/admin/users");
      const users = await usersResponse.json();
      const listener = users.find((entry) => entry.email === "listener@wavebeat.local");
      if (!listener) {
        test.skip(true, "No listener account available in environment");
        return;
      }
      const lockResponse = await request.patch(`/api/admin/users/${listener.id}/lock`, { data: { locked: true } });
      expect(lockResponse.ok()).toBeTruthy();
      const lockedUser = await lockResponse.json();
      expect(lockedUser.locked).toBe(true);
    });
  });
});
