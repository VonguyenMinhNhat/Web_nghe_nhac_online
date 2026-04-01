const { defineConfig } = require("@playwright/test");

module.exports = defineConfig({
  testDir: "./tests",
  workers: 1,
  timeout: 90_000,
  expect: {
    timeout: 15_000
  },
  reporter: [["list"]],
  use: {
    baseURL: process.env.BASE_URL || "http://localhost:8080",
    connectOptions: {
      wsEndpoint: `wss://cdp.lambdatest.com/playwright?capabilities=${encodeURIComponent(JSON.stringify({
        browserName: process.env.LT_BROWSER || "Chrome",
        browserVersion: process.env.LT_BROWSER_VERSION || "latest",
        "LT:Options": {
          platform: process.env.LT_PLATFORM || "Windows 11",
          build: process.env.LT_BUILD || "WaveBeat Functional Build",
          name: process.env.LT_TEST_NAME || "WaveBeat 20 functions",
          user: process.env.LT_USERNAME || "mrnhat2611",
          accessKey: process.env.LT_ACCESS_KEY || "LT_Ll0VaFkQr1oeu1G9oWf5jD0RMdiyakcZAxOw6sgGwMOdwAp",
          tunnel: true,
          network: true,
          video: true,
          console: true
        }
      }))}`
    },
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    video: "off"
  }
});
