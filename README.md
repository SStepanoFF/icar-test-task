# iCar API TAF (Vehicle Ordering)

Playwright (Java) `APIRequestContext` + JUnit 5 + AssertJ + Lombok + Allure reporting.

## Run
```bash
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1      # API tests need no browsers
export ICAR_API_TOKEN=...                      # if the API requires auth
mvn test -Denv=qa                              # env: dev (default) | qa | staging
mvn test -Denv=qa -Dgroups=smoke               # smoke only
mvn test -Dbase.url=http://localhost:8080/api  # override any property (e.g. a mock)
```

## Reporting (Allure)
Every run writes results to `target/allure-results`. Each HTTP call attaches its request and response body, and service methods are shown as steps.
```bash
mvn test -Denv=qa
mvn allure:serve        # build and open the report locally
mvn allure:report       # static report in target/site/allure-maven-plugin
```
The report's *Environment* widget shows the env the run used (`-Denv=...`, base URL, timeout, Java version); no secrets are written.
The CI workflow uploads both the report and the raw results as artifacts. Tests are grouped with `@Epic` / `@Feature`, smoke tests carry `@Severity(CRITICAL)`.

## Structure
| Layer | Class | Responsibility |
|---|---|---|
| config | `EnvConfig` | per-environment config, secrets from env vars |
| api | `BaseApi`, `ApiResponse` | base class: generic HTTP methods and JSON (de)serialization, no business knowledge |
| api/services | `OrdersApi extends BaseApi` | one method per endpoint, no assertions |
| api/dto | `OrderRequest`, `OrderResponse`, ... | request/response models |
| api/data | `OrderDataFactory` | valid default payloads; tests override only what matters |
| test/base | `BaseApiTest` | context lifecycle, preconditions via API, automatic cleanup |
| test/api | `OrdersApiTest` | tests and assertions only |

New endpoint group = new DTOs + one subclass of `BaseApi` (e.g. `InvoicesApi`); `BaseApi` is untouched.

## Assumptions (to confirm with REST docs / OpenAPI)
- Host is fictional (`*.icar.example.com`); tests need a real or mocked server to pass.
- Fields of DTOs, order statuses (`NEW/CONFIRMED/CANCELLED`) and error format `{code, message}` are illustrative.
- Status codes: POST 201, GET/PATCH 200, DELETE 200/204, missing resource 404, validation 400.
- Auth: Bearer token (optional).

## Test selection and trade-offs
5 tests: happy-path POST, GET, PATCH (partial update), DELETE + verify 404, one negative validation.
Each test is independent and creates its own data; cleanup is automatic.
Not covered (next steps): auth/permissions, concurrency on PATCH, invalid status transitions, schema/contract validation, idempotency.
