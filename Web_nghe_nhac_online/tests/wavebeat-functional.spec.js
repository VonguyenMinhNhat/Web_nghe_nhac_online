const { test, expect } = require("@playwright/test");
const cases = require("./fixtures/test-cases");
const { waitForDashboard, songCard } = require("./helpers/ui-helpers");

test.describe.serial("WaveBeat 20 functional cases", () => {
  const unique = Date.now();
  const user = {
    username: `user${unique}`,
    fullName: `User ${unique}`,
    email: `user${unique}@wavebeat.local`,
    password: "pass123",
    nextPassword: "pass456"
  };

  async function loginAsAdmin(request) {
    await request.post("/api/auth/login", {
      data: { email: "demo@wavebeat.local", password: "demo123" }
    });
  }

  for (const item of cases) {
    test(`${item.id} - ${item.name}`, async ({ page, request }) => {
      switch (item.id) {
        case "F01": {
          const response = await request.post("/api/auth/register", {
            data: {
              username: user.username,
              fullName: user.fullName,
              email: user.email,
              password: user.password
            }
          });
          expect(response.status()).toBe(201);
          const body = await response.json();
          expect(body.user.email).toBe(user.email);
          break;
        }

        case "F02": {
          await request.post("/api/auth/logout");
          const response = await request.post("/api/auth/login", {
            data: { email: user.email, password: user.password }
          });
          expect(response.ok()).toBeTruthy();
          const body = await response.json();
          expect(body.message).toBe("Login successful");
          break;
        }

        case "F03": {
          const response = await request.post("/api/auth/logout");
          expect(response.ok()).toBeTruthy();
          const me = await request.get("/api/users/me");
          expect(me.status()).toBe(401);
          break;
        }

        case "F04": {
          const forgot = await request.post("/api/auth/forgot-password", {
            data: { email: user.email }
          });
          expect(forgot.ok()).toBeTruthy();
          const payload = await forgot.json();
          expect(payload.resetCode).toContain("RESET-");
          const reset = await request.post("/api/auth/reset-password", {
            data: {
              email: user.email,
              resetCode: payload.resetCode,
              newPassword: user.nextPassword
            }
          });
          expect(reset.ok()).toBeTruthy();
          break;
        }

        case "F05": {
          const response = await request.put("/api/users/me", {
            data: {
              username: `${user.username}_updated`,
              fullName: `${user.fullName} Updated`,
              email: user.email
            }
          });
          expect(response.ok()).toBeTruthy();
          const body = await response.json();
          expect(body.username).toContain("_updated");
          break;
        }

        case "F06": {
          const response = await request.post("/api/users/me/change-password", {
            data: {
              currentPassword: user.nextPassword,
              newPassword: user.password
            }
          });
          expect(response.ok()).toBeTruthy();
          await loginAsAdmin(request);
          break;
        }

        case "F07": {
          await waitForDashboard(page);
          const cardCount = await page.locator(".song-card").count();
          expect(cardCount).toBeGreaterThanOrEqual(20);
          await expect(page.getByTestId("song-count")).toHaveText(String(cardCount));
          break;
        }

        case "F08": {
          await waitForDashboard(page);
          await page.getByTestId("search-input").fill("City Lights");
          await expect(page.locator(".song-card")).toHaveCount(1);
          await expect(songCard(page, "City Lights")).toBeVisible();
          break;
        }

        case "F09": {
          const response = await request.get("/api/songs/1");
          expect(response.ok()).toBeTruthy();
          const song = await response.json();
          expect(song.title).toBe("City Lights");
          expect(song.artist).toBe("Luna Grey");
          expect(song.album).toBe("Afterglow");
          break;
        }

        case "F10": {
          await waitForDashboard(page);
          await songCard(page, "City Lights").getByRole("button", { name: /Phat|Dang phat/ }).click();
          await expect(page.getByTestId("now-playing-title")).toHaveText("City Lights");
          await expect(page.getByTestId("play-btn")).toHaveText("Dung");
          break;
        }

        case "F11": {
          await waitForDashboard(page);
          await songCard(page, "City Lights").getByRole("button", { name: /Phat|Dang phat/ }).click();
          await expect(page.getByTestId("play-btn")).toHaveText("Dung");
          await page.getByTestId("play-btn").click();
          await expect(page.getByTestId("play-btn")).toHaveText("Phat");
          await page.getByTestId("progress-bar").evaluate((element) => {
            element.value = "35";
            element.dispatchEvent(new Event("input", { bubbles: true }));
          });
          await expect(page.getByTestId("progress-bar")).toHaveValue("35");
          break;
        }

        case "F12": {
          await waitForDashboard(page);
          await songCard(page, "City Lights").getByRole("button", { name: /Phat|Dang phat/ }).click();
          await page.getByTestId("next-btn").click();
          await expect(page.getByTestId("now-playing-title")).toHaveText("Sunset Drive");
          await page.getByTestId("prev-btn").click();
          await expect(page.getByTestId("now-playing-title")).toHaveText("City Lights");
          break;
        }

        case "F13": {
          await waitForDashboard(page);
          await page.getByTestId("volume-bar").evaluate((element) => {
            element.value = "0.25";
            element.dispatchEvent(new Event("input", { bubbles: true }));
          });
          await expect(page.getByTestId("volume-bar")).toHaveValue("0.25");
          await page.getByTestId("mute-btn").click();
          await expect(page.getByTestId("mute-btn")).toHaveText("Mo tieng");
          break;
        }

        case "F14": {
          await waitForDashboard(page);
          const targetCard = songCard(page, "Midnight Flow");
          await targetCard.getByRole("button", { name: /Phat|Dang phat/ }).click();
          await expect(page.getByTestId("detail-favorite-btn")).toBeVisible();
          await page.getByTestId("detail-favorite-btn").click();
          await page.getByTestId("filter-favorites").click();
          await expect(songCard(page, "Midnight Flow")).toBeVisible();
          break;
        }

        case "F15": {
          await waitForDashboard(page);
          await page.getByTestId("playlist-input").fill("Playwright Mix");
          await page.getByTestId("create-playlist-btn").click();
          await expect(page.getByTestId("playlist-list")).toContainText("Playwright Mix");
          break;
        }

        case "F16": {
          const createResponse = await request.post("/api/playlists", {
            data: { name: "API Regression Playlist" }
          });
          const playlist = await createResponse.json();

          const addResponse = await request.post(`/api/playlists/${playlist.id}/songs/2`);
          let updated = await addResponse.json();
          expect(updated.songIds).toContain(2);

          const removeResponse = await request.delete(`/api/playlists/${playlist.id}/songs/2`);
          updated = await removeResponse.json();
          expect(updated.songIds).not.toContain(2);
          break;
        }

        case "F17": {
          await request.post("/api/songs/3/play");
          const response = await request.get("/api/history");
          expect(response.ok()).toBeTruthy();
          const history = await response.json();
          expect(history[0].songTitle).toBe("Ocean Echo");
          break;
        }

        case "F18": {
          const comment = await request.post("/api/songs/3/comments", {
            data: { message: "Playwright comment", rating: 5 }
          });
          expect(comment.status()).toBe(201);
          const rating = await request.post("/api/songs/3/rating", {
            data: { rating: 4 }
          });
          const song = await rating.json();
          expect(song.totalRatings).toBeGreaterThan(0);
          break;
        }

        case "F19": {
          await loginAsAdmin(request);
          const createSong = await request.post("/api/admin/songs", {
            data: {
              title: "Playwright Song",
              artistId: 1,
              albumId: 1,
              genre: "Pop",
              durationSeconds: 200,
              plays: 0,
              trending: false,
              audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
            }
          });
          expect(createSong.status()).toBe(201);
          const created = await createSong.json();

          const updateSong = await request.put(`/api/admin/songs/${created.id}`, {
            data: {
              title: "Playwright Song Updated",
              artistId: 1,
              albumId: 1,
              genre: "Pop",
              durationSeconds: 210,
              plays: 5,
              trending: true,
              audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
            }
          });
          const updated = await updateSong.json();
          expect(updated.title).toBe("Playwright Song Updated");

          const deleted = await request.delete(`/api/admin/songs/${created.id}`);
          expect(deleted.ok()).toBeTruthy();
          break;
        }

        case "F20": {
          await loginAsAdmin(request);
          const genreRes = await request.post("/api/admin/genres", {
            data: { name: "Genre" }
          });
          expect(genreRes.ok()).toBeTruthy();

          const artistRes = await request.post("/api/admin/artists", {
            data: { name: "Artist", country: "VN", imageUrl: "img" }
          });
          const artist = await artistRes.json();

          const albumRes = await request.post("/api/admin/albums", {
            data: { title: `Album${unique}`, artistId: artist.id, releaseYear: 2026, coverColor: "#ffffff" }
          });
          const album = await albumRes.json();

          const usersResponse = await request.get("/api/admin/users");
          const users = await usersResponse.json();
          const listener = users.find((entry) => entry.email === "listener@wavebeat.local");
          const lockResponse = await request.patch(`/api/admin/users/${listener.id}/lock`, {
            data: { locked: true }
          });
          expect(lockResponse.ok()).toBeTruthy();

          const deleteAlbum = await request.delete(`/api/admin/albums/${album.id}`);
          expect(deleteAlbum.ok()).toBeTruthy();
          const deleteArtist = await request.delete(`/api/admin/artists/${artist.id}`);
          expect(deleteArtist.ok()).toBeTruthy();
          break;
        }

        default:
          throw new Error(`Case ${item.id} is not mapped.`);
      }
    });
  }
});
