# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This is a test-only automation project — there is no application source (`src/main` was intentionally deleted). All code lives under `src/test`. It targets the "StockBroker Demo" web app (a React/Vite SPA) running locally at `http://localhost:5173/` (start that app separately before running tests — this repo does not run it), plus its versioned REST API at `http://localhost:4100/api/v1` (see "REST API" below) for the `@api`-tagged scenarios. The UI has no stable `id`/`data-testid` attributes outside the login form (`#email`, `#password`, `button[type='submit']`); everything else must be located by visible text, `href`, or Tailwind utility classes.

Known app shape (Dashboard, post-login, see `DashboardPage`): a header with logout button and user email; top nav with `Dashboard`/`Trade`/`Payments`/`Inbox`/`Profile` links (`<a href="/...">` under `<header>`, an unread-count badge on Inbox); a welcome heading; three summary values (wallet balance, portfolio value, total gain/loss); a Holdings table (Ticker/Qty/Avg cost/Price/Value/Gain-loss); a Recent trades list; and four quick-link tiles under `<main>` that reuse the same `href`s as the nav (disambiguate by scoping the CSS selector to `header` vs `main`, not by text). Dashboard data (summary values, holdings, recent trades) loads asynchronously after the route renders — wait on a real value (e.g. a holding ticker appearing), not just element presence, before asserting on it.

Known app shape (Trade, see `TradePage`): a search input (`#search`, filters the company table live) above a company table (Ticker/Company/Sector/Price + Buy/Sell buttons per row); a "Your trade history" list below it; and a right-hand panel that cycles through three states as you interact — empty placeholder → order-builder form (quantity input `#quantity`, estimated total, wallet balance, "Review order"/"Cancel") → confirm screen (quantity/quoted price/estimated total, "Confirm buy"|"Confirm sell"/"Back"). That panel's price/total re-renders on a live tick, so its locators go through `TradePage.waitFor(...)` (a short `WebDriverWait`) rather than a bare `findElement`. Also: the "Buy order"/"Sell order" panel title renders the verb and " order" as separate text nodes (interpolated + static), so XPath `text()='Buy order'` never matches even though it looks like one string in `getPageSource()` — use `normalize-space()` instead; this bit us once already, so default to `normalize-space()` over `text()=` for any multi-word label on this app.

Known app shape (Payments, see `PaymentsPage`): a wallet balance card (same label/value pattern as elsewhere: `p` + `following-sibling::p`); two payment-method tabs, "Bank transfer" and "Debit card", toggling which fields render below `#amount` — bank transfer adds `#accountNumber`/`#ifsc`, debit card adds `#cardNumber`/`#expiry`/`#cvv` — both share the same `#amount` field and the same "Add funds" submit button; and a "Transaction history" table (Date/Description/Method/Status/Amount) that lists both payments *and* trades in one unified ledger (`Method` column is `TRADE` for buy/sell entries).

Known app shape (Inbox, see `InboxPage`): a two-pane layout — a message list (left, `main ul.divide-y > li > button` rows, each with subject/type badge `INVOICE`|`SYSTEM`/timestamp, plus an unread dot rendered only for unread messages) and a detail panel (right) for whichever message is selected, with type badge/subject `h2`/timestamp located via `preceding-sibling`/`following-sibling` off the one `<h2>` on the page, a body paragraph, and — only for `INVOICE` messages — a PDF link (`a[href*='/api/invoices/']`); a "Mark as read"/"Mark as unread" button toggles the selected message's read state. Selecting *any* message marks it read and decrements the Inbox nav badge count as a side effect — be aware assertions on that badge will shift after `selectMessage()`.

Known app shape (Profile, see `ProfilePage`): a read-only "Account" section as a `<dl>` (Email, Member since, KYC status badge — `dt`/`following-sibling::dd` pairs) and an "Edit details" form (`#name`/`#phone`/`#address`, "Save changes" submit) that shows an inline status banner (`div.bg-emerald-50` on success, `div.bg-red-50` on error) after submit.

All five post-login pages (Dashboard, Trade, Payments, Inbox, Profile) now have page objects.

Known app shape (Login/Register, see `LoginPage`/`RegisterPage`): the login screen has a "Don't have an account? Sign up" link (`a[href='/register']`, `LoginPage.goToSignUp()`) to the registration screen, which has `#name`/`#email`/`#phone`/`#password` and a "Create account" submit, plus an "Already have an account? Log in" link (`a[href='/login']`) back. Submitting a valid registration auto-logs-in and redirects straight to `/dashboard` as the new user — no separate login step needed afterward. Verified end-to-end with a real (throwaway, timestamp-suffixed) account; no cleanup endpoint exists for created demo accounts, so registration tests will accumulate users in the demo backend over time — that's expected for this environment.

## REST API

The app is served by two separate backend processes, discovered by reading the source monorepo directly (`D:/AutomationProjects/Applications/StockBroker/packages/*` on this machine — not part of this git repo, so treat this section as the record of what was found, not something to re-derive from this repo):

- **`packages/server`, port 4000** — the SPA's own backend (`/api/auth`, `/api/dashboard`, `/api/wallet`, `/api/companies`, `/api/holdings`, `/api/trades`, `/api/invoices`, `/api/inbox`), cookie/session-authenticated, single-origin CORS locked to the web app. Not what the API tests target.
- **`packages/api`, port 4100** — a separate, versioned, general-purpose REST API (`/api/v1/*`), explicitly built for external clients (permissive CORS, a Swagger UI at `/api/v1/docs`, an OpenAPI doc at `/api/v1/openapi.json`) with its own JWT bearer-token auth. **This is what `src/test/.../api` and the `@api`-tagged feature files exercise.**

`/api/v1` shape, JWT bearer auth (`Authorization: Bearer <token>`, obtained from `POST /users` or `POST /sessions`, 7-day expiry):

| Method & path | Auth | Notes |
|---|---|---|
| `POST /users` | none | Register. 201 `{accessToken, user}`. 409 if email taken. |
| `GET /users/me` | yes | Current user profile. |
| `PUT /users/me` | yes | Partial update (`name`/`phone`/`address`, all optional). |
| `POST /sessions` | none | Login. 201 `{accessToken, user}`. 401 on bad credentials. |
| `GET /wallet/balance` | yes | `{balance}`. |
| `POST /payments` | yes | Add funds; body is a discriminated union on `method`: `BANK_TRANSFER` (`accountNumber`, `ifsc`) or `DEBIT_CARD` (`cardNumber` — Luhn-checked, `expiry` MM/YY, `cvv`). 201 `{balance}`. |
| `GET /companies?search=` | yes | Catalog, optional ticker/name/sector filter. |
| `GET /trades` / `POST /trades` | yes | List, or place a `{companyId, type: BUY\|SELL, quantity}` order — price is always server-simulated, never client-supplied. 400 on insufficient balance/holdings. |
| `GET /portfolio` / `GET /portfolio/holdings` | yes | Cash + stock value summary, or just holdings. |
| `GET /invoices/by-trade/:tradeId` | yes | 404 if the trade isn't yours (tenant isolation). |
| `GET /invoices/:invoiceId/pdf` | yes | Binary `application/pdf`. |
| `GET /inbox` / `GET /inbox/:id` | yes | Registration seeds a `SYSTEM` "Welcome to StockBroker Demo" message. |

Error shape is uniform: `{"error": "...", "fieldErrors"?: {"<field>": "..."}}` — `fieldErrors` present for 400s (Zod validation or business-rule `HttpError`s), absent for other statuses.

**REST Assured gotcha:** build request specs via `RestAssured.given()` (see `ApiClient.request()`), not `new RequestSpecBuilder().build()` — the latter throws `NullPointerException: Cannot get property 'assertionClosure' on null object` from inside REST Assured's Groovy internals on the very first request, regardless of REST Assured version (reproduced on 5.4.0 and 5.5.1) or Java 21. This cost real debugging time; don't reintroduce it.

## Test coverage

`mvn test` now runs 45 scenarios / 232 steps: 15 UI scenarios / 88 steps (`smoke`, `login`, `registration`, `dashboard`, `trade`, `payments`, `inbox`, `profile`) plus 30 API scenarios / 144 steps (`api/auth`, `api/wallet_payments`, `api/companies_trades`, `api/portfolio`, `api/inbox`, `api/invoices`, all tagged `@api`) — all currently green (see "Known flaky UI tests" below for two pre-existing exceptions unrelated to the API work). Run `-Dcucumber.filter.tags="@api"` for API-only (no browser needed) or `"not @api"` for UI-only (no API service needed).

Per the earlier "execute mutating actions for real" decision: `trade.feature` actually buys 1 share of AAPL, `payments.feature` actually adds $10 via bank transfer, and `profile.feature` actually overwrites the phone number, every run. There is no reset/seed endpoint, so the demo account's wallet balance, holdings, and profile fields drift with every test run — expected, not a bug. `registration.feature` similarly leaves a new throwaway account behind each run (see above). The `@api` scenarios follow the same convention (real buys, real payments) but each registers its own fresh throwaway account via `ApiAuthSteps`/`ApiContext`, so they never touch or drift the shared UI test account — safe to run repeatedly without coordinating with the UI suite.

### Known flaky UI tests

Two UI scenarios were observed to fail intermittently across repeated `mvn test` runs, with different scenarios and error types each time (`registration.feature`'s sign-up link `NoSuchElementException`, `inbox.feature`'s mark-as-read toggle assertion, `trade.feature`'s company list `IndexOutOfBoundsException`, `login.feature`'s `#email` `NoSuchElementException`) — consistent with the documented lack of a reset endpoint and consequent backend state drift, not a regression from the API work (confirmed by re-running UI-only and seeing different scenarios fail each time). Investigate if this becomes a recurring blocker; not investigated further here since it predates and is orthogonal to the REST API test addition.

Two things worth knowing before adding more scenarios:
- **List-backed content needs an explicit wait, not just a bare `findElement(s)`.** Every page that renders a list/table asynchronously after the route mounts (`DashboardPage.getHoldings()`, `TradePage.getCompanies()`/`clickBuy`/`clickSell`, `PaymentsPage.getTransactionHistory()`, `InboxPage.getMessages()`/`selectMessage()`) now waits on `presenceOfElementLocated` internally before reading. This was a real, reproducible failure during initial test-writing (`ArrayIndexOutOfBoundsException` / `NoSuchElementException` reading the list one line after navigating to the page) — the earlier manual exploration never hit it because it always had a `Thread.sleep()` after navigation that real step definitions don't have. If a new page-object method reads rows/text right after a navigation or tab-switch, give it the same treatment.
- **Submit actions that show a "Processing…" disabled-button state are asynchronous** (`TradePage.confirmOrder()`, `PaymentsPage.addFunds()`, `ProfilePage.saveChanges()`) and now block internally until the resulting status message appears, so callers don't need arbitrary sleeps. If a new mutating action follows this same UI pattern, wait for its status message the same way rather than sleeping in the step definition.

## Build system

- Maven (`pom.xml`), groupId `org.bharathi`, artifactId `stock-broker-java-test`, packaging `jar`.
- Java 21, UTF-8 source encoding.
- Stack: Selenium 4 (WebDriver) + Cucumber 7 (BDD/Gherkin) + TestNG (runner/assertions).
- Browser driver binaries are resolved automatically by Selenium Manager (built into Selenium 4.6+) — no `webdrivermanager` dependency and no manual driver downloads.

## Common commands

Run these from the repository root (`pom.xml` location). The app/API under test must already be running (`baseUrl`/`apiBaseUrl` of the selected environment) — this repo does not start them.

- Run the full suite against DEV (default): `mvn test`
- Run against a different environment: `mvn test -Denv=FTA` or `-Denv=INT` (must key-match an entry in `src/test/resources/environments.json`)
- Run a single scenario by name: `mvn test -Dcucumber.filter.name="Successful login"`
- Run by Cucumber tag: `mvn test -Dcucumber.filter.tags="@yourTag"`
- API scenarios only, no browser needed: `mvn test -Dcucumber.filter.tags="@api"`
- UI scenarios only, no API service needed: `mvn test -Dcucumber.filter.tags="not @api"`
- One-off override without touching the file: `mvn test -Dbase.url=... -Dapi.base.url=... -Dtest.email=... -Dtest.password=...`

## Architecture

- `src/test/resources/environments.json` — external per-environment config, one block per environment name (`DEV`, `FTA`, `INT`, ...) each with `baseUrl`/`apiBaseUrl`/`testEmail`/`testPassword`. Edit this file to point tests at a different environment or update credentials — no code changes needed, just a rebuild/rerun to pick up the change (it's a resource file, not compiled source). `FTA`/`INT` are currently empty placeholders; fill them in when those environments' details are known.
- `src/test/java/org/bharathi/config/ConfigReader.java` — picks the environment block via `-Denv` (default `DEV`), loads `environments.json` from the classpath (or an arbitrary file on disk if `-Dconfig.file=...` is passed), and exposes `getBaseUrl()`/`getApiBaseUrl()`/`getTestEmail()`/`getTestPassword()`. Each also accepts a matching `-D` system property (`base.url`/`api.base.url`/`test.email`/`test.password`) as a one-off override that wins over the file.
- `src/test/resources/features/*.feature` — Gherkin scenarios (BDD) for the UI.
- `src/test/resources/features/api/*.feature` — Gherkin scenarios for the REST API, every `Feature:` tagged `@api` (see "REST API" above).
- `src/test/java/org/bharathi/stepdefinitions/` — Cucumber step definitions. UI: one class per feature area (`SmokeSteps`, `LoginSteps`, ...), fetching the shared driver via `DriverManager.getDriver()` and driving the UI through page objects rather than raw locators. API: `Api*Steps` classes (`ApiAuthSteps`, `ApiWalletPaymentsSteps`, `ApiCompaniesTradesSteps`, `ApiPortfolioSteps`, `ApiInboxSteps`, `ApiInvoicesSteps`) calling the `api/` clients, plus `ApiCommonSteps` for generic, reusable response assertions (status code, JSON field equality/existence, validation-error messages) shared across every API feature file — prefer adding a new generic assertion there over a bespoke one-off `Then` step.
- `src/test/java/org/bharathi/pages/` — Page Object classes (`LoginPage`, `RegisterPage`, `DashboardPage`, `TradePage`, `PaymentsPage`, `InboxPage`, `ProfilePage`) encapsulating locators/actions for one screen. Add a new page object here per screen rather than putting `By` locators directly in step definitions.
- `src/test/java/org/bharathi/api/` — REST Assured API clients, one class per `/api/v1` resource (`UsersApi`, `SessionsApi`, `WalletApi`, `PaymentsApi`, `CompaniesApi`, `TradesApi`, `PortfolioApi`, `InvoicesApi`, `InboxApi`) mirroring `pages/`'s one-class-per-screen convention — each method returns the raw REST Assured `Response` and lets the caller assert, rather than asserting internally. `ApiClient.request()`/`authenticatedRequest(token)` build the base `RequestSpecification` (baseUri from `ConfigReader.getApiBaseUrl()`, JSON content type, bearer token) — **always build via `RestAssured.given()`** (see the REST Assured gotcha above). Add a new API client here per resource rather than calling REST Assured directly from step definitions.
- `src/test/java/org/bharathi/api/ApiContext.java` — `ThreadLocal`-backed scenario state (access token(s), last response, noted company/trade/invoice/message ids) shared across the `Api*Steps` classes, mirroring how `DriverManager` shares one `WebDriver` across UI step-definition classes — necessary because Cucumber instantiates each step-definition class separately per scenario, so plain instance fields don't cross class boundaries.
- `src/test/java/org/bharathi/driver/DriverManager.java` — `ThreadLocal<WebDriver>` holder so parallel scenario execution doesn't share browser sessions; `getDriver()` lazily creates a `ChromeDriver`, `quitDriver()` tears it down.
- `src/test/java/org/bharathi/hooks/Hooks.java` — Cucumber `@Before`/`@After` hooks. `setUp`/`tearDown` open/close the browser around every UI scenario (`@Before("not @api")`/`@After("not @api")` — `@api`-tagged scenarios don't need or get a browser); `resetApiContext` clears `ApiContext` after every `@api` scenario. Add new cross-cutting setup/teardown here rather than in step definitions.
- `src/test/java/org/bharathi/runners/TestRunner.java` — `AbstractTestNGCucumberTests` entry point; `@CucumberOptions` wires `features`/`glue` and must list every glue package (step defs + hooks) or new step classes silently won't be picked up. (The `api/` package holds API *clients*, not glue, so it doesn't need to be listed — only `stepdefinitions`/`hooks` do.)
- `testng.xml` (repo root) — TestNG suite invoked by the `maven-surefire-plugin` config in `pom.xml`; this is what `mvn test` actually runs.

When adding a new UI feature area, the usual pattern is: new `.feature` file → new page object in `pages/` for the screen it exercises → new step definition class in `stepdefinitions/` that reuses `DriverManager` + the page object → nothing else needs to change (the runner's `glue` package covers the whole package).

When adding a new API feature area, the pattern is: new `.feature` file under `features/api/`, tagged `@api` → new API client in `api/` for the resource it exercises (or extend an existing one) → new `When`/`Given` steps in the relevant `Api*Steps` class (or a new one) that call the client and store the response via `ApiContext.setLastResponse(...)` → reuse `ApiCommonSteps`' generic `Then` steps for assertions wherever they fit, rather than writing bespoke ones.
