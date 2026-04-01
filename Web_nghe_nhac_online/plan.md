Problem statement

The application has flaky behavior, missing tests, outdated dependencies, fragile database handling, and incomplete CI and docs. Several runtime errors and poor error handling reduce reliability and slow development.

Proposed approach (phases)

1. Discovery & Triage
- Run the app, collect logs, reproduce critical failures, and list high-impact bugs.
- Create failing test cases that capture observed bugs.

2. Testing Coverage
- Add unit tests for core services (auth, DB, business logic).
- Add integration/E2E tests (API endpoints, UI flows) and stable test fixtures.

3. Fixes & Hardening
- Fix root causes identified in discovery: DB connection leaks, auth bugs, null checks.
- Improve error handling and add input validation.

4. Dependency & DevOps
- Update dependencies, fix breaking changes, enable reproducible builds.
- Add or improve CI pipeline (run tests, lint, build).

5. Performance & Cleanup
- Profile hotspots, optimize queries/caching, remove dead code.
- Improve documentation and contributor setup instructions.

Todos

- discovery-report: Run discovery & triage
  - Reproduce key failures locally; collect spring-boot logs, browser logs, and capture steps.
  - Produce short report listing top 5 issues with reproduction steps and priority.

- add-tests-e2e: Add end-to-end tests
  - Implement E2E tests for main user flows (login, stream playback, playlist management) using existing Playwright setup.
  - Create stable fixtures and seed data.

- add-unit-tests-core: Add unit tests for core services
  - Add unit tests for authentication, user service, and database repositories.
  - Mock external systems and ensure isolation.

- fix-authentication: Fix authentication/authorization bugs
  - Investigate login/session/token handling. Add missing null-checks and proper error responses.
  - Add tests to prevent regressions.

- fix-db-connection: Fix DB connection and SQL issues
  - Verify connection pooling settings, fix SQL errors, add retries where appropriate.
  - Add integration tests against local test DB.

- improve-error-handling: Improve error handling and logging
  - Standardize error response shapes, add context-rich logs, and avoid leaking stack traces to clients.

- modernize-deps: Update dependencies safely
  - Upgrade major dependencies, run test suite, and address breaking changes. Pin versions in package.json/pom.xml.

- add-ci: Add/Improve CI pipeline
  - Ensure CI runs tests, linter, and builds. Add caching and artifact storage for test reports.

- update-docs-setup: Update contributor and setup docs
  - Document local setup, running tests, and troubleshooting steps. Include SQL server startup docs.

- perf-profiling: Profile and optimize performance
  - Run profiler, identify slow DB queries or memory hotspots, and implement caching or query optimization.
