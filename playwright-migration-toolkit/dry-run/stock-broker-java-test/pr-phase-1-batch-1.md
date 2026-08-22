# Migration batch PR: phase-1/batch-1

**Branch**: `playwright-migration/phase-1-batch-1` → `master` | **Commit**: `ba57117` (base `f3fac79`)
**Worktree**: `C:\Users\Bharathiraja\IdeaProjects\stock-broker-java-test-playwright-phase-1-batch-1`

- **Migration Plan**: `playwright-migration-toolkit/dry-run/stock-broker-java-test/migration-plan-2026-08-22.md` (`stock-broker-java-test-migration-plan-v2`) — implements phase-1/batch-1
- **Framework Profile**: `playwright-migration-toolkit/dry-run/stock-broker-java-test/framework-profile-2026-08-22.md`

## What this batch does

Introduces the Playwright side of the parallel-run migration, scoped to the smallest possible surface per the plan: driver lifecycle + login. Nothing in the legacy Selenium suite is touched or affected — `mvn test` still runs the legacy suite exactly as before.

**New files** (`org.bharathi.playwright.*`, fully separate from legacy `org.bharathi.*`):

| File | Lines | Purpose |
|---|---|---|
| `driver/PlaywrightContextManager.java` | +78 | Replaces `DriverManager`'s role — one process-wide `Playwright`/`Browser`, `ThreadLocal<Page>` + fresh `BrowserContext` per scenario |
| `hooks/Hooks.java` | +31 | Same `@Before("not @api")`/`@After("not @api")` tag-scoping as legacy `Hooks`; opens/closes a `BrowserContext` instead of a `WebDriver` |
| `pages/LoginPage.java` | +57 | 1:1 structural port of legacy `LoginPage` — stable `#email`/`#password`/`button[type='submit']` locators, no changes needed |
| `stepdefinitions/LoginSteps.java` | +42 | Same Gherkin step text as legacy. `iShouldBeLoggedIn()` inline-checks the header "Log out" button (20s wait) rather than calling a Playwright `DashboardPage`, which doesn't exist yet — that's phase-2/batch-1's job. Commented as a scoped, temporary substitute. |
| `stepdefinitions/SmokeSteps.java` | +43 | Same Gherkin step text as legacy |
| `runners/PlaywrightTestRunner.java` | +22 | New `AbstractTestNGCucumberTests` entry point, glue = `org.bharathi.playwright.{stepdefinitions,hooks}` only |
| `testng-playwright.xml` | +9 | New suite file, scoped to `login.feature`+`smoke.feature` |

**Modified files**: `pom.xml` only (+20/-2) —
- Added `com.microsoft.playwright:playwright:1.49.0` dependency (additive, doesn't touch `selenium-java`/`rest-assured`/anything else)
- Promoted surefire's hardcoded `<suiteXmlFile>testng.xml</suiteXmlFile>` to an overridable `${suiteXmlFile}` property, **defaulting to the unchanged literal `testng.xml`** — plain `mvn test` behavior is byte-for-byte unchanged; the new suite runs only via explicit `mvn test -DsuiteXmlFile=testng-playwright.xml`

**Not touched**: `login.feature`, `smoke.feature` (Gherkin unchanged — same scenarios drive both suites), `environments.json`, any legacy `org.bharathi.{driver,hooks,pages,stepdefinitions,runners}` file, any CI/CD config (none exists in-repo).

## Definition of Done (governance/POLICY.md)

- [x] Builds and passes in isolation — `mvn test-compile` clean; `mvn test -DsuiteXmlFile=testng-playwright.xml` → 2 scenarios / 5 steps, all passing
- [x] `migration-reviewer` pre-review: **Clean**, 2026-08-22 — no findings raised
- [x] `migration-verifier` Parity Report: `playwright-migration-toolkit/dry-run/stock-broker-java-test/parity-reports/phase-1-batch-1.md` — verdict: **Parity Confirmed** (2/2 scenarios, 4 runs total — 2 legacy + 2 migrated — all passing, no flakiness reproduced)
- [x] Scope matches the Migration Plan's batch description exactly — `PlaywrightContextManager` + `Hooks` + `LoginPage` + `login.feature`/`smoke.feature` glue, nothing more (the `pom.xml`/`testng-playwright.xml`/`PlaywrightTestRunner` additions are the necessary minimum plumbing to make the above buildable/runnable, explicitly flagged by both the executor and reviewer, not scope drift)
- [x] No edits to the legacy suite's files — confirmed via `git diff f3fac79 ba57117 --stat`, no `org/bharathi/{driver,hooks,pages,stepdefinitions,runners}` (legacy) path appears
- [x] No CI/CD, shared config, or secrets touched — `environments.json` untouched; no CI/CD file exists in-repo to touch
- [x] This PR title/body references the batch ID — `phase-1/batch-1`

## Human reviewer sign-off

- [ ] I reviewed the full diff, not just the checklist above
- [ ] Reviewer: _____ | Date: _____

## Known, deliberate scope carve-outs (from Gate D)

- REST Assured API layer: out of scope for this whole migration
- CI pipeline: not added
- Plaintext DEV credentials in `environments.json`: not remediated here, tracked separately
- `DashboardPage`'s `text()=` locator fragility: not in this batch anyway (phase-2/batch-1); Gate D decided **port as-is**, not fix, when that batch lands
