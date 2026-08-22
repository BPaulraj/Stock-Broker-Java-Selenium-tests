# Parity Report: stock-broker-java-test — phase-1/batch-1

Produced by: `migration-verifier` | Date: 2026-08-22 | Legacy run: 2026-08-22 12:11 IST
(two consecutive runs, both green) | Migrated run: 2026-08-22 12:1x IST (two consecutive
runs, both green, run immediately after the legacy runs completed)

## Verdict: PARITY CONFIRMED

Both scenarios in this batch's scope (`login.feature`'s "Successful login with valid
credentials" and `smoke.feature`'s "Application loads") pass in both the legacy Selenium
suite and the migrated Playwright suite, with equivalent coverage, equivalent (in one
case intentionally-scoped-but-equivalent) assertions, and equivalent outcomes across two
consecutive runs of each. No parity gaps found. One pre-existing, already-flagged
scope note is called out below for visibility but is not a gap — see "Notes."

## Scope

- Batch/scenarios covered: phase-1/batch-1 per the Migration Plan
  (`playwright-migration-toolkit/dry-run/stock-broker-java-test/migration-plan.md`,
  §4) — `PlaywrightContextManager` (replaces `DriverManager`), `Hooks`, `LoginPage`,
  and the two scenarios in `login.feature` + `smoke.feature`:
  - "Successful login with valid credentials" (`login.feature`)
  - "Application loads" (`smoke.feature`)
- Legacy suite: master repo, branch `master`, commit `f3fac79` (HEAD at time of this
  check), run via `mvn test "-Dcucumber.filter.name=Successful login with valid
  credentials|Application loads"` (no `@login`/`@smoke` tags exist on these feature
  files, so scenario-name regex was used instead of `-Dcucumber.filter.tags`, per the
  task's own suggestion).
- Migrated suite: worktree `stock-broker-java-test-playwright-phase-1-batch-1`, branch
  `playwright-migration/phase-1-batch-1`, commit `ba57117`, run via
  `mvn test -DsuiteXmlFile=testng-playwright.xml` (already scoped to exactly these two
  feature files via `PlaywrightTestRunner`'s `@CucumberOptions(features = {...})`).
- Sequencing used: **sequential, not concurrent** — legacy suite run to completion
  first (twice), then the migrated suite run (twice), against the same shared DEV
  account (`nanthini.c@demo.com`, per `environments.json`, identical in both repos
  content-wise — only line-ending differs, see Notes). Both scenarios in scope are
  read-only from the app's perspective (login + page-load; no logout, no trade/payment/
  profile mutation), so — unlike `trade.feature`/`payments.feature`/`profile.feature` —
  there was no actual mutating-state hazard here per the `parity-verification` skill's
  sequencing rules; sequential execution was still used out of caution (avoiding two
  browsers driving the same account concurrently) even though the shared-account-drift
  concern the skill warns about doesn't really apply to this particular batch's
  scenarios.
- App under test: started locally for this check (`npm run dev` in
  `D:/AutomationProjects/Applications/StockBroker`, web on :5173, server on :4000) since
  it was not already running; stopped afterward (full process tree killed) — nothing
  left running that this check didn't start.

## Scenario-by-scenario comparison

| Scenario | Legacy result | Migrated result | Coverage parity | Assertion parity | Outcome parity | Notes |
|---|---|---|---|---|---|---|
| Application loads (smoke.feature) | Pass (2/2 runs) | Pass (2/2 runs) | Yes | Yes — identical: `driver.getTitle().contains("StockBroker")` vs `page.title().contains("StockBroker")` | Yes | 1:1 port, no behavior change |
| Successful login with valid credentials (login.feature) | Pass (2/2 runs) | Pass (2/2 runs) | Yes | Yes, with a documented scope note — see below | Yes | See "Notes" |

## Gaps found (if any)

None.

## Notes on environmental factors

- **Assertion-parity detail on the login scenario (not a gap):** legacy
  `LoginSteps.iShouldBeLoggedIn()` delegates to `DashboardPage.isDisplayed(Duration)`,
  which waits up to 20s for `//header//button[normalize-space()='Log out']` to become
  visible. The migrated `LoginSteps.iShouldBeLoggedIn()` doesn't call a Playwright
  `DashboardPage` (that class doesn't exist yet — its conversion is phase-2/batch-1's
  job per the Migration Plan) and instead inlines an equivalent check: Playwright
  `page.locator("header button", hasText("Log out"))` asserted visible with a 20s
  timeout via `assertThat(...).isVisible(...)`. This is the same element, same
  semantics (header-scoped button containing the text "Log out", visibility-waited,
  same 20s budget), so assertion parity holds. The migrated code itself carries an
  inline comment flagging this as an intentional, scoped decision to be revisited when
  phase-2/batch-1 lands a real Playwright `DashboardPage` — this is a good practice
  worth noting positively, not a defect, and does not block this batch.
- **Feature files are byte-identical in content** between the two repos (both
  `login.feature` and `smoke.feature` diff clean once line-ending normalization
  — LF in the master repo vs CRLF in the worktree — is accounted for); confirms the
  Gherkin itself was not touched during conversion, as the Migration Plan/executor
  commit message claims.
- **`environments.json` is likewise content-identical** (same line-ending-only diff),
  so both suites ran against the same DEV target/credentials — no config drift.
- **Stability check:** each suite was run twice back-to-back (legacy twice, then
  migrated twice) specifically to rule out the kind of intermittent flakiness
  documented in CLAUDE.md's "Known flaky UI tests" section. That section names four
  specific flaky failure modes, none of which are in this batch's two scenarios
  (registration's sign-up-link `NoSuchElementException`, inbox mark-as-read, trade's
  company-list `IndexOutOfBoundsException`, and *login's own* `#email`
  `NoSuchElementException`). Worth flagging: login's `#email` locator is one of the
  four documented flaky failure points, but it did not reproduce in either suite across
  4 total runs (2 legacy + 2 migrated) here — consistent with the flakiness being
  attributed to backend state drift rather than a deterministic defect, not evidence
  it's fixed. If it resurfaces on a future run, treat it as inherited pre-existing
  flakiness per the `parity-verification` skill's noise-vs-gap table, not a new
  migration-introduced defect, unless it starts failing consistently in the migrated
  suite specifically.
- **No mutating side effects in this batch's scope**, so side-effect parity (rule 4 of
  the toolkit's parity definition) is trivially satisfied — neither scenario writes to
  wallet balance, holdings, or profile fields.
