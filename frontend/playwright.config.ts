import { defineConfig, devices } from '@playwright/test';

const frontendUrl = 'http://127.0.0.1:5173';

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: false,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: process.env.CI ? [['github'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: frontendUrl,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'], channel: 'chromium' } },
  ],
  webServer: [
    {
      name: 'backend',
      command: '../backend/mvnw -f ../backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=local',
      url: 'http://127.0.0.1:8080/actuator/health',
      timeout: 120_000,
      reuseExistingServer: !process.env.CI,
      env: { ...process.env, DB_PASSWORD: process.env.DB_PASSWORD ?? 'joblens' },
      stdout: 'pipe',
      stderr: 'pipe',
    },
    {
      name: 'frontend',
      command: 'npm run dev -- --host 127.0.0.1',
      url: frontendUrl,
      timeout: 60_000,
      reuseExistingServer: !process.env.CI,
      stdout: 'pipe',
      stderr: 'pipe',
    },
  ],
});
