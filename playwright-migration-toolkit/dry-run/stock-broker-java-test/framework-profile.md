# Framework Profile: stock-broker-java-test

Produced by: `framework-analyzer` (dry run, executed manually against direct repo
knowledge rather than via the live Agent tool — see note at file end) | Date: 2026-08-22
| Repo/commit: `stock-broker-java-test` @ `82a94a4`

> Confidence is High throughout — every claim below is from direct inspection of the
> repo's own source (`pom.xml`, `CLAUDE.md`, page objects, step definitions, hooks,
> config, feature files), not inference, since this repo was the one this session's
> earlier work was already deeply embedded in.

## 1. Language & build tooling — confidence: High

- Language: Java 21, UTF-8 source encoding
- Build tool: Maven (`pom.xml`), groupId `org.bharathi`, artifactId
  `stock-broker-java-test`
- Key dependencies: `selenium-java` 4.27.0, `cucumber-java`/`cucumber-testng` 7.20.1,
  `testng` 7.10.2, `org.json:json` 20250107, `io.rest-assured:rest-assured` 5.5.1
  (added for the API layer)

## 2. Test runner & BDD layer — confidence: High

- Runner: TestNG, via `AbstractTestNGCucumberTests` (`TestRunner.java`), driven by
  `testng.xml` at repo root, invoked through `maven-surefire-plugin`
- BDD layer: Cucumber 7, `.feature` files under `src/test/resources/features/` (UI) and
  `src/test/resources/features/api/` (API, all tagged `@api`)
- Step definitions live in `src/test/java/org/bharathi/stepdefinitions/`, one class per
  feature area, matching Cucumber's package-recursive glue scanning
  (`org.bharathi.stepdefinitions` covers subpackages too, though this repo keeps
  everything flat by class-name convention rather than subpackages)

## 3. Browser automation library — confidence: High

- UI layer: Selenium 4 WebDriver, `ChromeDriver` specifically, resolved via Selenium
  Manager (no `webdrivermanager` dependency, no manual driver downloads)
- API layer: **not browser automation** — REST Assured against a separate versioned
  REST API (`/api/v1/*`, JWT bearer auth, port 4100). Per `framework-discovery`'s
  guidance, this layer doesn't need Playwright's browser automation at all; Playwright's
  `APIRequestContext` could host it if the org wants everything under one library, but
  it isn't required — flagged as an open question for the Migration Plan.

## 4. Design pattern — confidence: High

- Page Object Model, done properly: one class per screen
  (`LoginPage`, `RegisterPage`, `DashboardPage`, `TradePage`, `PaymentsPage`,
  `InboxPage`, `ProfilePage`) in `src/test/java/org/bharathi/pages/`. Locators as
  `private static final By` fields, constructor takes `WebDriver`, action methods
  return `this` or navigate to the next page's object. No raw `By` locators found
  leaking into step definitions — pattern is consistent across all seven page objects.
- The new API layer (added this session) mirrors the same one-class-per-resource
  convention deliberately: `src/test/java/org/bharathi/api/` has one client class per
  `/api/v1` resource (`UsersApi`, `SessionsApi`, `WalletApi`, `PaymentsApi`,
  `CompaniesApi`, `TradesApi`, `PortfolioApi`, `InvoicesApi`, `InboxApi`), each method
  returning the raw REST Assured `Response` rather than asserting internally — same
  "encapsulate the interaction, let the caller assert" philosophy as the page objects.

## 5. Reusable components — confidence: High

- Driver lifecycle: `DriverManager` (`ThreadLocal<WebDriver>`), lazily creates a
  `ChromeDriver`, torn down via `quitDriver()`
- API session/scenario state: `ApiContext` (`ThreadLocal`-backed), holding access
  token(s), last response, and noted ids (company/trade/invoice/message) across
  step-definition classes within one scenario — added this session, explicitly modeled
  on `DriverManager`'s pattern because Cucumber instantiates each step-def class
  separately per scenario, so cross-class state needs a `ThreadLocal` holder either way
- Wait helpers: no generic custom wait utility class; waits are inlined per-page-object
  where needed (`TradePage.waitFor(...)`, a short `WebDriverWait` for a
  live-re-rendering panel; `presenceOfElementLocated` waits inline in list-reading
  methods like `DashboardPage.getHoldings()`)
- Generic API assertions: `ApiCommonSteps` — a shared step-definition class (not a
  "component" in the page-object sense, but functionally the API suite's reusable
  assertion layer) providing status-code, JSON-field-equality, and validation-error
  `Then` steps reused across all 6 API feature files
- No data builder/factory classes; test data is either a configured shared account
  (UI) or a freshly-registered throwaway account per scenario (API, and one UI flow —
  registration)

## 6. Config & environment management — confidence: High

- Mechanism: `src/test/resources/environments.json`, one block per environment name
  (`DEV`, `FTA`, `INT`), each with `baseUrl`/`apiBaseUrl`/`testEmail`/`testPassword`
- Read by `ConfigReader` (`src/test/java/org/bharathi/config/ConfigReader.java`), which
  picks the environment via `-Denv` (default `DEV`) and exposes
  `getBaseUrl()`/`getApiBaseUrl()`/`getTestEmail()`/`getTestPassword()`, each
  overridable by a matching `-D` system property (`base.url`/`api.base.url`/
  `test.email`/`test.password`)
- `FTA`/`INT` are currently empty placeholders, not yet populated
- Secrets handling: `testEmail`/`testPassword` for the shared DEV account live directly
  in `environments.json` in plaintext, checked into the repo. This is a real observation
  worth a human's attention (it's a demo/POC credential, not flagged here as a severity
  judgment — just recording the mechanism honestly, no values reproduced here per
  the secrets guardrail)

## 7. Test data management — confidence: High

- UI suite: predominantly one shared, configured DEV account, mutated for real across
  runs (`trade.feature` really buys 1 share, `payments.feature` really adds funds,
  `profile.feature` really overwrites the phone number) — no reset/seed endpoint, so
  wallet balance/holdings/profile fields drift run over run, which the repo's own
  `CLAUDE.md` documents as expected, not a bug. `registration.feature` additionally
  leaves a throwaway account behind each run (timestamp-suffixed email, no cleanup
  endpoint).
- API suite: every `@api` scenario registers its own fresh throwaway account
  (`ApiAuthSteps.registerFreshAccount()`, timestamp+nanotime-suffixed email) —
  deliberately avoids sharing or drifting the UI suite's configured account.

## 8. Locator strategy — confidence: High

- Stable `id`/`data-testid`-equivalent attributes exist **only** on the login form
  (`#email`, `#password`, `button[type='submit']`) — everywhere else in the app under
  test has no stable test hooks, per the repo's own `CLAUDE.md`.
- Elsewhere: visible text, `href` attributes (nav links, quick-link tiles), and
  Tailwind utility classes (status banners: `div.bg-emerald-50`/`div.bg-red-50`).
  XPath used selectively, with a documented gotcha: a "Buy order"/"Sell order" panel
  title renders as split text nodes, so `text()='Buy order'` never matches even though
  it looks like one string — the team's convention is to default to `normalize-space()`
  over `text()=` for any multi-word label, learned the hard way once already.
- Disambiguation between nav (`<header>`) and quick-link tiles (`<main>`) that reuse the
  same `href`s is done by CSS scoping (`header a[href='/trade']` vs
  `main a[href='/trade']`), not by text.

## 9. Parallelization — confidence: High

- `testng.xml` currently has no `parallel` attribute set — the suite runs
  single-threaded today. `DriverManager` and `ApiContext` are already `ThreadLocal`-based
  in anticipation of parallel execution, but it isn't switched on.

## 10. Reporting & CI — confidence: Medium

- Cucumber plugin config: `pretty`, `summary` (`TestRunner`'s `@CucumberOptions`) —
  console output only, no HTML/JSON report plugin currently configured.
- No CI pipeline definition found in the repo (no `.github/workflows`, no `Jenkinsfile`,
  no equivalent) — this appears to be a locally-run suite so far, not yet wired into CI.
  Confidence Medium rather than High since absence-of-evidence isn't fully conclusive
  without asking the human directly.

## 11. Known pain points — confidence: High (from the repo's own `CLAUDE.md`)

- List-backed content (holdings, company table, transaction history, trade history,
  inbox messages) loads asynchronously after route mount — reading it without an
  explicit wait caused a real, reproducible `ArrayIndexOutOfBoundsException`/
  `NoSuchElementException` during initial test-writing; every affected page-object
  method now waits internally (`presenceOfElementLocated`, or waiting on a real value
  rather than just element presence for Dashboard's summary numbers).
- Submit actions with a "Processing…" disabled-button state (`TradePage.confirmOrder()`,
  `PaymentsPage.addFunds()`, `ProfilePage.saveChanges()`) are asynchronous; each blocks
  internally until its status message appears, rather than the caller sleeping.
- REST Assured (added this session): building request specs via
  `new RequestSpecBuilder().build()` throws a Groovy internals
  `NullPointerException: Cannot get property 'assertionClosure' on null object` on the
  very first request — reproduced on both REST Assured 5.4.0 and 5.5.1, unrelated to
  request content. Fixed by building specs via `RestAssured.given()` instead
  (`ApiClient.request()`). Worth carrying forward as a known gotcha if any Playwright
  migration work touches the API layer's tooling choices at all (unlikely, since REST
  Assured isn't itself part of the Playwright migration scope, but noted since it cost
  real debugging time once already).
- UI suite flakiness observed this session, unrelated to the API work: two different
  scenarios failed on two separate full-suite runs (`registration.feature`'s sign-up
  link locator once, `login.feature`'s `#email` locator and `trade.feature`'s company
  list once), consistent with the documented lack of a reset endpoint and consequent
  backend state drift rather than a suite defect — worth the Migration Plan flagging as
  a risk area to watch during parity verification (a flaky legacy baseline makes
  parity comparison noisier, not wrong).

## 12. Scale — confidence: High

- 14 feature files, 45 scenarios, 232 steps total:
  - UI: 8 feature files (`smoke`, `login`, `registration`, `dashboard`, `trade`,
    `payments`, `inbox`, `profile`), 15 scenarios, 88 steps
  - API: 6 feature files (`api/auth`, `api/wallet_payments`, `api/companies_trades`,
    `api/portfolio`, `api/inbox`, `api/invoices`), 30 scenarios, 144 steps
- 7 UI page objects, 9 API client classes, ~14 step-definition classes

## Observations

- The API layer was added in this same session and already follows conventions
  (`ApiContext` mirroring `DriverManager`, generic assertion steps in `ApiCommonSteps`)
  that a Playwright migration should preserve the *spirit* of, even though the API
  layer itself likely stays on REST Assured rather than moving to Playwright's
  `APIRequestContext` — see Migration Plan open questions.
- The plaintext DEV credentials in `environments.json` are a pre-existing pattern, not
  introduced by this profile's analysis — noted for the human reviewer's awareness
  going into migration planning, not as a finding this profile is scoped to fix.

---

**Note on how this dry run was produced**: this Framework Profile was produced by
directly following `agents/framework-analyzer.md`'s discovery checklist against
first-hand knowledge of this repo from earlier work in the same session, rather than by
invoking `framework-analyzer` through Claude Code's `Agent` tool with a fresh context —
custom agent/skill definitions are picked up at session start, so a newly-added
`.claude/agents/framework-analyzer.md` in this repo wouldn't be selectable mid-session
without restarting. The output format, content, and rigor are what the live agent would
produce; only the invocation mechanism differs. To run this for real end-to-end in a
new project, install the toolkit per the top-level `README.md` and start a fresh
session there.
