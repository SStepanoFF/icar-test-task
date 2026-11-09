# iCar API TAF (Vehicle Ordering)

Playwright (Java) `APIRequestContext` + JUnit 5 + AssertJ + Lombok.

## Run
```bash
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1      # API tests need no browsers
export ICAR_API_TOKEN=...                      # if the API requires auth
mvn test -Denv=qa                              # env: dev (default) | qa | staging
mvn test -Denv=qa -Dgroups=smoke               # smoke only
mvn test -Dbase.url=http://localhost:8080/api  # override any property (e.g. a mock)
```

## Structure
| Layer | Class | Responsibility |
|---|---|---|
| core/config | `EnvConfig` | per-environment config, secrets from env vars |
| core/api | `BaseApi`, `ApiResponse` | universal HTTP methods, JSON (de)serialization; no business knowledge |
| api/services | `OrdersApi extends BaseApi` | one method per endpoint, no assertions |
| api/dto | `OrderRequest`, `OrderResponse`, ... | request/response models |
| api/data | `OrderDataFactory` | valid default payloads; tests override only what matters |
| test/base | `BaseApiTest` | context lifecycle, preconditions via API, automatic cleanup |
| test/api | `OrdersApiTest` | tests and assertions only |

New endpoint group = new DTOs + one subclass of `BaseApi` (e.g. `InvoicesApi`); core is untouched.

## Assumptions (to confirm with REST docs / OpenAPI)
- Host is fictional (`*.icar.example.com`); tests need a real or mocked server to pass.
- Fields of DTOs, order statuses (`NEW/CONFIRMED/CANCELLED`) and error format `{code, message}` are illustrative.
- Status codes: POST 201, GET/PATCH 200, DELETE 200/204, missing resource 404, validation 400.
- Auth: Bearer token (optional).

## Test selection and trade-offs
5 tests: happy-path POST, GET, PATCH (partial update), DELETE + verify 404, one negative validation.
Each test is independent and creates its own data; cleanup is automatic.
Not covered (next steps): auth/permissions, concurrency on PATCH, invalid status transitions, schema/contract validation, idempotency.
