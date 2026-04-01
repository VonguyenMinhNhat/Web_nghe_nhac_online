async function waitForDashboard(page) {
  await Promise.all([
    page.waitForResponse((response) => response.url().includes("/api/dashboard") && response.ok()),
    page.waitForResponse((response) => response.url().includes("/api/songs") && response.ok()),
    page.goto("/")
  ]);
  await page.getByTestId("song-grid").waitFor();
}

function songCard(page, title) {
  return page.locator(".song-card").filter({ has: page.getByRole("heading", { name: title }) }).first();
}

module.exports = {
  waitForDashboard,
  songCard
};
