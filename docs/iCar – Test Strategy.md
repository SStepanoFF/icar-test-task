# iCar – Test Strategy

**Scope and context.** iCar is a dealership vehicle lifecycle platform: a PowerBuilder desktop UI over REST APIs, a 2-week release cycle, 8 engineers and 3 QA. Today there are about 10 automated UI tests and about 50 API tests, and manual regression takes about 5 days per release. Escaped defects cluster in Vehicle Ordering, Purchase/Invoice processing and Supplier/OEM integrations. This strategy moves confidence earlier and cheaper without slowing delivery.

## Principles

- **Risk first.** Effort follows business impact and defect history, not feature count.
- **Push tests down.** Prefer API and contract tests over UI tests: they are faster, more stable and pinpoint failures.
- **Shared ownership.** Quality is a team responsibility; tests live next to the code and have a named owner.

## Manual testing approach

- Exploratory testing with written charters for each release, focused on changed areas of ordering, invoicing and integrations.
- Manual regression becomes a short, risk-based checklist driven by change impact, not a full pass every release.
- Manual testers also design and review automated cases, so knowledge moves into the suite.
- Production defects are analysed and turned into automated tests whenever possible.

## API testing approach

- Main automation layer, built with Playwright (Java) `APIRequestContext`, JUnit 5 and AssertJ.
- Layered framework: a `BaseApi` base class, one service class per endpoint group extending it, DTOs, test-data factories, per-environment configuration.
- Coverage per endpoint: happy path, validation and negative cases, boundary values, status transitions, permissions, idempotency where relevant.
- Independent tests: each creates its own data through the API and cleans up after itself. Results are published as Allure reports with request/response attachments.
- **Contract testing** for OEM/supplier integrations: consumer-driven contracts or OpenAPI schema validation, plus stubs for external systems.

## UI automation approach

- Keep UI automation small: 3-5 critical end-to-end smoke paths for ordering.
- The existing tool (TestComplete) stays for the PowerBuilder desktop application.
- Fix flakiness before adding tests: explicit waits and stable object identification, isolated test data, quarantine for unreliable tests so they do not block releases.
- Test data and preconditions are prepared through the API instead of through the UI, which makes tests shorter and more stable.

## CI/CD quality gates (GitHub Actions)

| Stage | What runs | Gate |
| --- | --- | --- |
| Pull request | Build, unit tests, API smoke (target under 5 min) | Blocking |
| Merge to main / nightly | Full API regression and contract tests | Blocking for release candidate |
| Release candidate | Full API and UI suites, risk-based manual checks, defect review | Go / no-go decision |
| After release | Production smoke and monitoring, defect review | Feedback into next cycle |

Flaky tests are quarantined and tracked, never silently ignored.

## Regression strategy

- **P0 (every build, automated):** ordering end-to-end flow, invoice creation, order submission to OEM/supplier.
- **P1 (every release, mostly automated):** ordering variants and edge cases, invoice calculations and states, integration contracts, plus targeted manual checks chosen by change impact.
- **P2 (rotating and exploratory):** low-risk and rarely used functions, covered on a rotation.
- Every escaped defect produces a new automated test at the right layer.
- Manual cases that are fully covered by automation are retired, so the manual suite keeps shrinking.

## Success metrics

Targets are proposals and must be adjusted once the baseline from the first two weeks is known.

| Metric | Target |
| --- | --- |
| Manual regression duration | 5 days to about 2 days at month 3, then 1-2 days |
| Escaped defects in ordering, invoicing and integrations | About 30% fewer by month 3 and 50-60% fewer by month 6. Critical and high severity: about 70% fewer within two quarters |
| API coverage of critical endpoints (happy and negative paths) | 80% or more |
| Flaky UI test rate | Under 3% reruns |
| CI feedback time | Smoke under 5 min, full API suite under 15 min |
| Tests with a named owner | 100% |
| Defects found before release vs total | Rising trend |

## Framework evolution&#32;

The delivered framework covers the Vehicle Ordering API only, as the assignment requires. It is designed to grow, and the following steps are intentionally not implemented here:

- **UI layer** in the same framework: `BasePage` with Page Objects over a driver interface, reusing the shared configuration, data factories and API preconditions. PowerBuilder is a native desktop application, so Playwright cannot drive it; the options are to keep TestComplete or to run a time-boxed spike on Appium with WinAppDriver (depends on whether the controls are exposed to Windows UI Automation).
- More service classes (invoices, suppliers) added by extending `BaseApi`, without changing the base class.
- A contract-testing module for OEM/supplier integrations and stubs.
- Parallel execution and managed test data. (Allure reporting is already included in the delivered framework.)

## Assumptions and risks of this plan

- API contracts and DTO fields in the Сode Part are illustrative until confirmed with the real REST documentation.
- Targets depend on baseline data and on developers sharing test ownership.
- The UI tooling decision depends on the outcome of the PowerBuilder spike.
