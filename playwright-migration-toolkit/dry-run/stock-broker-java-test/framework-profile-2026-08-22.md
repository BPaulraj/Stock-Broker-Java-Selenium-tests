# Framework Profile: stock-broker-java-test

Produced by: `framework-analyzer` | Date: 2026-08-22 | Repo/commit: `stock-broker-java-test` @ `f3fac79`

> This is a fresh, independent discovery pass (not a copy of the existing example at `playwright-migration-toolkit/dry-run/stock-broker-java-test/framework-profile.md`, which was produced at an earlier commit `82a94a4`). Findings below were verified against current source; where they diverge from or add to that earlier example, it's called out explicitly. Confidence is High throughout unless noted — nearly everything here is direct inspection of `pom.xml`, source files, feature files, and config, not inference.

## 1. Language & build tooling — confidence: High

- Language: Java 21, UTF-8 source encoding (`pom.xml` `maven.compiler.source/target` = 21)
- Build tool: Maven, groupId `org.bharathi`, artifactId `stock-broker-java-test`, packaging `jar`
- Key dependency versions (from `pom.xml` `<dependencies>`, not inferred): `selenium-java` 4.27.0, `cucumber-java`/`cucumber-testng` 7.20.1, `testng` 7.10.2, `org.json:json` 20250107, `io.rest-assured:rest-assured` 5.5.1. Build plugins: `maven-compiler-plugin` 3.13.0, `maven-surefire-plugin` 3.5.2 (configured to run `testng.xml`).
- No `src/main` exists — confirmed via directory listing; this is genuinely test-only, matching CLAUDE.md's claim.

## 2. Test runner & BDD layer — confidence: High

- Runner: TestNG via `AbstractTestNGCucumberTests` (`org/bharathi/runners/TestRunner.java`), invoked by `testng.xml` (single `<test>` wrapping `TestRunner`) through `maven-surefire-plugin`.
- BDD layer: Cucumber 7. `.feature` files under `src/test/resources/features/*.feature` (UI, 8 files) and `src/test/resources/features/api/*.feature` (API, 6 files, every `Feature:` tagged `@api` — confirmed by grep, no exceptions).
- `@CucumberOptions` glue = `{"org.bharathi.stepdefinitions", "org.bharathi.hooks"}`; plugin = `{"pretty", "summary"}`.
- Step definitions live in one flat package (`org.bharathi.stepdefinitions`), one class per feature area for both UI (`LoginSteps`, `TradeSteps`, `DashboardSteps`, `InboxSteps`, `PaymentsSteps`, `ProfileSteps`, `RegisterSteps`, `SmokeSteps`) and API (`ApiAuthSteps`, `ApiWalletPaymentsSteps`, `ApiCompaniesTradesSteps`, `ApiPortfolioSteps`, `ApiInboxSteps`, `ApiInvoicesSteps`, plus shared `ApiCommonSteps`) — no subpackaging by feature.

## 3. Browser automation library — confidence: High

- UI: Selenium 4 WebDriver, `ChromeDriver` only (`DriverManager.getDriver()` hardcodes `new ChromeDriver()` — no browser-choice abstraction, no cross-browser config observed). Driver binaries resolved via Selenium Manager (Selenium 4.6+), no `webdrivermanager` dependency.
- API layer is **not browser automation**: REST Assured against a separate versioned REST API (`/api/v1`, JWT bearer, port 4100), confirmed via `ApiClient`/`UsersApi`/etc. This layer doesn't need Playwright's browser automation at all; Playwright's `APIRequestContext` could host it but nothing here forces that decision.

## 4. Design pattern — confidence: High

- **Page Object Model, done properly and consistently.** Sampled all 7 UI page objects (`LoginPage`, `RegisterPage`, `DashboardPage`, `TradePage`, `PaymentsPage`, `InboxPage`, `ProfilePage`): every one follows the same shape — `private static final By` locator fields, constructor takes `WebDriver`, action methods either fluently return `this`/void, query methods return primitives or small `record` types (`TradePage.Company`, `DashboardPage.Holding`, `PaymentsPage.Transaction`, `InboxPage.MessageSummary`). No raw `By` locators or `driver.findElement` calls found in any step-definition class sampled (`TradeSteps`, `DashboardSteps`, `LoginSteps`) — they instantiate a page object and call its methods only.
- The API client layer (`org/bharathi/api/`) deliberately mirrors this: one class per `/api/v1` resource (`UsersApi`, `SessionsApi`, `WalletApi`, `PaymentsApi`, `CompaniesApi`, `TradesApi`, `PortfolioApi`, `InvoicesApi`, `InboxApi`), each method returning the raw REST Assured `Response` and leaving assertion to the caller — same "encapsulate the interaction, let the caller assert" philosophy, applied to a non-browser layer.
- Assertions live in step definitions (`org.testng.Assert.*`), not inside page objects/API clients — consistent boundary.

## 5. Reusable components — confidence: High

- Driver lifecycle: `DriverManager` — `ThreadLocal<WebDriver>`, lazy `ChromeDriver` creation, `quitDriver()` teardown. Wired via `Hooks.setUp()`/`tearDown()` (`@Before("not @api")`/`@After("not @api")`).
- API scenario state: `ApiContext` — `ThreadLocal`-backed, holds access token(s), last `Response`, and noted ids (company/trade/invoice/message), explicitly modeled on `DriverManager`'s pattern to work around Cucumber instantiating step-def classes separately per scenario. Reset via `Hooks.resetApiContext()` (`@After("@api")`).
- Wait helpers: no shared/generic wait utility class. Each page object that needs one inlines its own private `waitFor(By)` (`TradePage`, `InboxPage`, `PaymentsPage`) wrapping a `WebDriverWait` + `presenceOfElementLocated`, duplicated per class rather than centralized in a base class or helper.
- Shared assertion utilities: `ApiCommonSteps` provides generic, reusable `Then` steps (status code, JSON field equality/greater-than/not-null, error-message-contains, field-level validation-error-contains) shared across all 6 API feature files — this is the closest thing to a shared assertion library in the repo. No equivalent generic UI assertion helper exists; UI assertions are ad hoc per step class.
- No data builder/factory classes anywhere (UI or API) — test data is either the one configured shared account (UI) or literal/inline values in step definitions (e.g. `ApiAuthSteps.uniqueEmail()`/hardcoded `"TestPass123!"`).

## 6. Config & environment management — confidence: High

- Mechanism: `src/test/resources/environments.json`, one JSON block per environment name (`DEV`, `FTA`, `INT`), each with `baseUrl`/`apiBaseUrl`/`testEmail`/`testPassword`. Loaded by `ConfigReader` via classpath (or `-Dconfig.file=...` for an arbitrary file on disk).
- `ConfigReader` selects environment via `-Denv` (default `DEV`), fails fast with a clear message if the requested key is missing from the JSON. Each of `getBaseUrl()`/`getApiBaseUrl()`/`getTestEmail()`/`getTestPassword()` is individually overridable by a matching `-D` system property (`base.url`/`api.base.url`/`test.email`/`test.password`), which wins over the file.
- `FTA` and `INT` blocks exist but all four fields are empty strings — not usable today, confirmed by reading the file directly.
- **Secrets handling**: `DEV`'s `testEmail`/`testPassword` are stored in plaintext directly inside `environments.json`, which is tracked in git (not in `.gitignore` — confirmed by reading `.gitignore`, which excludes `target/`, IDE files, etc., but not this resource file). Mechanism only is recorded here per the secrets guardrail; no value is reproduced in this profile.

## 7. Test data management — confidence: High

- UI suite: predominantly **one shared, configured DEV account**, mutated for real on every run — `trade.feature` buys 1 share, `payments.feature` adds funds via bank transfer, `profile.feature` overwrites the phone number. No reset/seed endpoint exists, so wallet balance/holdings/profile fields drift run-over-run by design (documented in CLAUDE.md, confirmed structurally: no cleanup call anywhere in the step defs or hooks). `registration.feature` additionally creates a fresh throwaway account each run (timestamp-suffixed email) with no cleanup path.
- API suite: **fresh throwaway account per scenario** — `ApiAuthSteps.registerFreshAccount()`/`uniqueEmail()` (`System.currentTimeMillis()` + `System.nanoTime()` suffix) registers a new account via `POST /users` for essentially every `@api` scenario that needs auth, explicitly avoiding any interaction with the UI suite's shared account.
- No DB seeding, no fixture files, no test-data JSON/CSV inputs found anywhere in `src/test/resources`.

## 8. Locator strategy — confidence: High

- Stable `id`/accessibility attributes exist **only** on the login form (`#email`, `#password`, `button[type='submit']`) and a handful of form inputs elsewhere (`#search`, `#quantity`, `#amount`, `#accountNumber`/`#ifsc`/`#cardNumber`/`#expiry`/`#cvv`, `#name`/`#phone`/`#address`) — confirmed across all page objects. Everything else (buttons, headings, table rows, nav links, status banners) is located by visible text (`normalize-space()` XPath predominantly), `href` attributes, or Tailwind utility classes (`div.bg-emerald-50`/`div.bg-red-50` for status banners, `span.rounded-full`/`span.bg-brand-600` for inbox badges).
- Locators are centralized as `private static final By` fields per page object — no duplication of locator strings across classes observed in the sampled files.
- Disambiguation of identically-hrefed elements (nav `<header>` links vs quick-link tiles under `<main>`) is done by CSS scoping (`header a[href='/trade']` vs `main a[href='/trade']`), not by text — a deliberate, documented pattern in `DashboardPage`.
- **New finding not in the prior dry-run profile**: `DashboardPage`'s three summary-value locators (`WALLET_BALANCE_VALUE`, `PORTFOLIO_VALUE`, `TOTAL_GAIN_LOSS_VALUE`) use exact-match `text()='Wallet balance'` / `text()='Portfolio value'` / `text()='Total gain / loss'` — all multi-word labels — rather than `normalize-space()`. This is the same class of locator CLAUDE.md warns about ("Buy order"/"Sell order" bit the team once because multi-word labels can render as split text nodes, hence "default to `normalize-space()` over `text()=` for any multi-word label"), but `DashboardPage` predates that lesson (it's part of the original commit) and was never retrofitted. It currently works, but it's a latent fragility inconsistent with the codebase's own stated convention — worth a human's attention, not something I changed. See Observations.

## 9. Parallelization — confidence: High

- No parallel execution configured anywhere: `testng.xml` has no `parallel` attribute, no `cucumber.properties`/`junit-platform.properties` file exists in the repo (confirmed via glob — none found), and no thread-count settings appear in `pom.xml`'s surefire config. The suite runs fully single-threaded today.
- `DriverManager` and `ApiContext` are both `ThreadLocal`-based, which would support parallelization if enabled, but nothing currently turns it on.

## 10. Reporting & CI — confidence: High (report format) / Medium (CI absence)

- Cucumber plugin config is `pretty` + `summary` only — console output, no HTML/JSON Cucumber report plugin (no `cucumber-reporting`, no Allure dependency in `pom.xml`) configured.
- TestNG/Surefire produces its own XML reports under `target/surefire-reports` (per `maven-surefire-plugin` default behavior and confirmed by the README: "Test reports are written to `target/surefire-reports`").
- No CI pipeline definition found anywhere in the repo — checked for `.github/workflows/*`, `Jenkinsfile`, and any `*.yml` at all (glob for `**/*.yml` returned nothing). No downstream dashboard/Slack/quality-gate integration is evident from the repo itself. Confidence is Medium rather than High on "no CI exists" specifically, since absence of an in-repo config doesn't rule out an externally-defined pipeline (e.g., a Jenkins job configured outside this repo) — worth confirming directly with the human rather than assuming greenfield.

## 11. Known pain points — confidence: High (from CLAUDE.md, cross-checked against source)

- Async-loaded list content (holdings, company table, transaction history, inbox messages) caused real, reproducible `NoSuchElementException`/`ArrayIndexOutOfBoundsException` failures during initial test-writing when read without an explicit wait; every affected page-object method (`DashboardPage.getHoldings()`, `TradePage.getCompanies()`/`clickBuy`/`clickSell`, `PaymentsPage.getTransactionHistory()`, `InboxPage.getMessages()`/`selectMessage()`) now waits internally via `presenceOfElementLocated` before reading — confirmed present in all listed methods.
- Submit actions with an async "Processing…" disabled-button state (`TradePage.confirmOrder()`, `PaymentsPage.addFunds()`, `ProfilePage.saveChanges()`) block internally on the resulting status message rather than requiring callers to sleep — confirmed in each page object's method body.
- REST Assured gotcha: `new RequestSpecBuilder().build()` throws a Groovy-internals `NullPointerException` on the first request; `ApiClient.request()` avoids it by always using `RestAssured.given()`. Confirmed no code anywhere uses `RequestSpecBuilder` (grep returned zero matches) — the fix has been applied consistently.
- Two UI scenarios exhibit intermittent flakiness across repeated runs (different scenario/error each time — sign-up link, mark-as-read toggle, company list index, `#email` lookup), attributed to backend state drift from the lack of a reset/seed endpoint, not a suite defect. This is a real risk area for parity verification during migration: a flaky legacy baseline makes before/after comparison noisier.
- `FTA`/`INT` environments are unpopulated placeholders — the suite has effectively only ever run against `DEV`.

## 12. Scale — confidence: High

- 37 Java files under `src/test/java`, ~1,932 lines total (page objects + step definitions + API clients + config/driver/hooks/runner, measured directly via `wc -l`).
- 14 feature files (8 UI, 6 API) under `src/test/resources/features`, 336 lines of Gherkin.
- 45 scenarios total (verified by counting `Scenario:` occurrences), matching CLAUDE.md's claim: 15 UI / 30 API.
- 186 explicit step lines counted directly in the `.feature` files (`Given`/`When`/`Then`/`And`/`But`); CLAUDE.md's claimed 232 *executed* steps is plausibly higher because `Background:` steps (e.g. dashboard.feature's 3-step Background, shared by 3 scenarios) are written once but execute once per scenario — this reconciles rather than contradicts the written count, but wasn't independently verified by running the suite.
- 7 UI page objects, 9 API client classes, 14 step-definition classes (8 UI + 6 API, `ApiCommonSteps` counted among the 6).

## Observations

- **Locator inconsistency, not previously flagged**: `DashboardPage`'s three summary-value locators use `text()=` on multi-word labels ("Wallet balance", "Portfolio value", "Total gain / loss"), which is exactly the pattern CLAUDE.md documents as bitten-once-already elsewhere in the codebase (recommending `normalize-space()` instead). It currently passes, so this is not a live bug, but it's a latent fragility and a drift from the team's own stated convention — worth a human's attention before/during migration, since a Playwright rewrite is a natural point to either fix it or deliberately carry the same locator logic forward.
- **Committed plaintext credentials**: `environments.json`'s `DEV` block, which is tracked in git, contains the shared UI test account's password in plaintext. This is a pre-existing pattern (not introduced by this analysis) but worth surfacing explicitly for the human reviewer, since a migration is a natural point to move to environment variables or a secrets manager instead of carrying the file forward as-is.
- **A prior Framework Profile already exists in this repo** at `playwright-migration-toolkit/dry-run/stock-broker-java-test/framework-profile.md` (produced manually at commit `82a94a4`, one commit before the toolkit itself was added). It's largely consistent with this independent pass — same stack, same POM pattern, same test-data strategy — which is a useful cross-check. The one substantive addition this pass found that the earlier one didn't note is the `DashboardPage` `text()=` locator inconsistency above. The human reviewer may want to diff the two rather than treat this one as a full replacement.
- No generic/base page-object class or shared wait utility exists — each page object reimplements its own `waitFor` helper where needed (`TradePage`, `InboxPage`, `PaymentsPage`), and three others (`LoginPage`, `ProfilePage`, `RegisterPage`, `DashboardPage`) don't have one at all despite some of them containing async-sensitive reads (e.g. `DashboardPage.getHoldings()` inlines its own `WebDriverWait` rather than reusing a shared pattern). Not a bug, just duplicated logic worth noting for anyone assessing how much "framework layer" is actually reusable versus per-page boilerplate.

---

**Summary**: This is a Java 21 / Maven repo combining a properly-and-consistently-applied Selenium 4 + Cucumber 7 (BDD) + TestNG UI suite (7 page objects, 8 feature files, 15 scenarios) with a structurally parallel REST Assured API suite (9 resource clients, 6 `@api`-tagged feature files, 30 scenarios) against a separate versioned REST API — both layers share the same "one class per unit, encapsulate the interaction, let the caller assert" design philosophy, with `ThreadLocal`-backed session state (`DriverManager`/`ApiContext`) as the main reusable infrastructure. Config is a simple external JSON file with system-property overrides; test data is a shared, deliberately-drifting UI account plus fresh-throwaway-account-per-scenario for the API suite; the suite runs single-threaded with no CI pipeline or rich report format currently wired up. Genuine risk areas for a migration to track are the two intermittently-flaky UI scenarios (backend state drift, not framework bugs), the plaintext committed test credentials, and one locator-strategy inconsistency in `DashboardPage` — all documented above for human review, none acted on.
