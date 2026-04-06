const { execSync } = require('child_process');
const LambdaTestTunnel = require('@lambdatest/node-tunnel');

const tunnel = new LambdaTestTunnel();
const tunnelArgs = {
  user: process.env.LT_USERNAME || "mrnhat2611",
  key: process.env.LT_ACCESS_KEY || "LT_Ll0VaFkQr1oeu1G9oWf5jD0RMdiyakcZAxOw6sgGwMOdwAp"
};

console.log("Starting LambdaTest tunnel...");
tunnel.start(tunnelArgs, (error, status) => {
  if (error) {
    console.error("Failed to start tunnel:", error);
    process.exit(1);
  } else {
    console.log("Tunnel started successfully. Status:", status);
    try {
      // Run the lambdatest script
      console.log("Running Playwright tests...");
      execSync('npx playwright test --config=playwright.lambdatest.config.js', { stdio: 'inherit' });
    } catch (e) {
      console.error("Tests failed.");
    } finally {
      tunnel.stop(() => {
        console.log("Tunnel stopped.");
        process.exit(0);
      });
    }
  }
});
