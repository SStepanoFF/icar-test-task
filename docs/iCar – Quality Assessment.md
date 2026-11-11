# iCar – Quality Assessment

## 1. Three highest quality risks

**R1. Vehicle Ordering integrity.** It is the most business-critical flow, used daily by every dealership, and the largest source of escaped defects. Protection is thin: about 50 API tests for the whole product and about 10 UI tests. A defect here stops dealerships from working, so both likelihood and impact are high.

**R2. Purchase and invoice processing.** Errors here are financial (wrong amounts, duplicate or missing invoices, wrong state after an order change). They are often noticed late, by finance or by the dealer, and are expensive to correct. This flow sits downstream of ordering, so every ordering change can silently affect it.

**R3. Supplier and OEM integrations without contract testing.** External systems change on their own schedule, and any mismatch in a field, status or format surfaces only in production. Manual regression cannot reliably reproduce these systems, so this area has the weakest feedback loop of all.

**Risk multiplier:** 5 days of manual regression plus flaky UI tests mean quality feedback arrives late and is only partly trusted. That pushes the team towards shipping on gut feeling, which is how defects escape.

**Manual-heavy coverage is itself a risk.** Manual regression is slow (about 5 days per release) and exposed to human error: repetitive checks get skipped or done inconsistently under time pressure. Broader automated coverage, starting with the highest-risk areas, is therefore critical rather than optional.

## 2. What stays primarily manual

The rule: automate what is repeatable, deterministic and high-risk; keep manual what needs human judgement or changes too often to pay back automation.

- Exploratory testing of new or changed ordering and invoicing features (time-boxed charters per release).
- Usability, layout and workflow feel of the desktop UI.
- Business scenarios whose rules are still unclear or changing, until they stabilise.
- First verification of a new OEM/supplier integration against its sandbox, and investigation of production defects.

## 3. What to automate first (in order)

1. **Vehicle Ordering API**: create, read, update, delete, validation, status transitions and business rules. It is the cheapest, fastest and most stable layer, and it covers the top risk.
2. **Purchase/Invoice API**: invoice creation from orders, calculations, state flow, and consistency after order changes.
3. **Contract tests for the top OEM/supplier integrations**: consumer-driven contracts or schema validation against OpenAPI, plus stubs so tests do not depend on external availability.
4. **CI wiring**: smoke on every PR, full API suite nightly, visible reports.
5. **Release sanity smoke tests**: the repeatable part of the release sanity check on realistic data, run automatically against every release candidate.
6. **UI layer, last and small**: stabilise the existing UI tests first, then add 3-5 end-to-end smoke paths for ordering, using API calls to prepare data (faster and less flaky than clicking through setup).

## 4. Prioritization for the first three months

| Period | Focus | Outcome |
| --- | --- | --- |
| Weeks 1-2: Discover | Analyse escaped defects of the last 3 months, or of the releases with the largest code and functional changes, by component and root cause. Build a risk map. Review existing tests (keep / fix / delete). Baseline metrics. Quick win: split the manual regression suite into P0/P1/P2 tiers. | Shared view of risk and a measured starting point |
| Weeks 3-6: Foundation and ordering | Build on the existing API framework: cover the Vehicle Ordering API and add a smoke subset that runs on every PR. Triage flaky UI tests: quarantine, then fix the worst offenders. Agree test ownership. Standing rule from now on: every defect, escaped or found in testing, gets an automated regression test. | Ordering protected by fast automated checks |
| Weeks 7-10: Expand | Purchase/Invoice API. Contract tests for 1-2 key integrations. Nightly full API run. | The three risk areas have automated coverage |
| Weeks 11-12: Reduce and review | Remove manual cases now covered by automation. Target regression of about 2 days. Review metrics and plan the next quarter. | Visible time saved, evidence for next steps |

## 5. Information I would request before finalising the approach

- Escaped-defect data (last 3 months or the most heavily changed releases): component, root cause, severity, how it was found.
- REST documentation (OpenAPI/Swagger) for ordering, purchase, invoice and integration endpoints, including error formats and status codes.
- Environments: how many, how stable, how test data is created and reset, and whether OEM/supplier sandboxes exist.
- Integration details: protocols, change frequency, who owns each contract, SLAs.
- The current manual regression suite: size, content, which cases are already covered by automation.
- Why UI tests are flaky (timing, data, environment, locators) and how often they run.
- Current CI duration, release process and who approves a release today.
- Who owns which tests today, and the team's appetite and capacity for developers to write API tests.
