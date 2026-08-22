---
name: framework-discovery
description: Checklist and signal matrix for identifying an unfamiliar test automation codebase's language, runner, BDD layer, browser-automation library, design pattern, and supporting infrastructure — across Java, JS/TS, Python, .NET, Ruby, and Robot Framework ecosystems. Load this before writing a Framework Profile, whenever the source stack isn't already known, or when in doubt about whether an assumption ("this is probably Selenium+TestNG") actually holds.
---

# Framework discovery

The goal is never to guess a framework from a file extension or a folder name. It is to
find the specific dependency declarations, config files, and code idioms that prove it,
because `migration-planner` needs a mapping table it can trust, not a hunch.

## Step 1 — language & build tool (always do this first)

| Signal file | Language | What to read from it |
|---|---|---|
| `pom.xml` | Java (Maven) | `<dependencies>` block — the real signal for everything else |
| `build.gradle` / `build.gradle.kts` | Java/Kotlin (Gradle) | `dependencies {}` block |
| `package.json` | JS/TS | `dependencies`/`devDependencies` |
| `requirements.txt`, `pyproject.toml`, `Pipfile` | Python | pinned packages |
| `*.csproj`, `packages.config` | .NET | `<PackageReference>` entries |
| `Gemfile` | Ruby | `gem` declarations |
| `*.robot` files present anywhere | Robot Framework (can pair with any of the above as its underlying interpreter) | `Library` imports inside the `.robot` files |

Read the *actual* dependency versions, not just names — a "Selenium" dependency could be
Selenium 3 (very different waiting/locator ergonomics) or Selenium 4 (closer to
Playwright's relative wait model already).

## Step 2 — test runner & BDD layer signal matrix

| Ecosystem | Runner-only signal | + BDD layer signal | Notes |
|---|---|---|---|
| Java | `testng` dep, `testng.xml` | `io.cucumber:cucumber-java` + `cucumber-testng` or `cucumber-junit`, `.feature` files, `@CucumberOptions` | This repo (`stock-broker-java-test`) is exactly this combination — see `dry-run/stock-broker-java-test/framework-profile.md` for a worked example |
| Java | `junit`/`junit-jupiter` dep | same Cucumber signals, `cucumber-junit-platform-engine` | |
| JS/TS | `mocha`, `jasmine`, `jest` dep | `@cucumber/cucumber` dep, `.feature` files | |
| JS/TS | `@playwright/test` dep already present | — | Already (partially) migrated, or a second suite already exists — flag this loudly, don't assume greenfield |
| JS/TS | `cypress` dep, `cypress.config.*` | rare (Cypress-Cucumber preprocessor plugin if present) | Cypress's architecture (in-browser runner, no WebDriver protocol) is the single biggest mapping-effort driver in this matrix — flag High complexity by default |
| JS/TS | `webdriverio` dep, `wdio.conf.*` | `@wdio/cucumber-framework` | |
| Python | `pytest` + `selenium` deps | `pytest-bdd` or `behave` dep, `.feature` files | |
| Python | `robotframework` dep | (Robot Framework's Gherkin-like syntax is native, not a separate BDD layer) | `SeleniumLibrary` import inside `.robot` files confirms Selenium underneath |
| .NET | `NUnit`/`xunit`/`MSTest` dep | `SpecFlow` dep, `.feature` files | |
| Ruby | `rspec`/`cucumber` gem | `cucumber` gem + `.feature` files | |

## Step 3 — browser automation library

Separate from the runner/BDD layer — a repo can pair almost any runner with almost any
of these:

- **Selenium WebDriver** — `WebDriver`/`RemoteWebDriver`/`ChromeDriver` types, `By`
  locators, explicit `WebDriverWait` usage. Check the Selenium major version (3 vs 4 —
  4 added relative locators and a closer-to-Playwright wait model).
- **Cypress** — no WebDriver protocol at all; runs inside the browser. `cy.` command
  chains. Highest structural distance from Playwright of anything in this list.
- **WebdriverIO** — WebDriver-protocol based like Selenium, but with its own fluent API
  (`$()`, `browser.`) — closer to Playwright's locator ergonomics than raw Selenium.
- **Puppeteer** — Chrome DevTools Protocol based, same lineage as Playwright
  conceptually (Playwright's own API was heavily influenced by Puppeteer) — often the
  *smallest* migration effort of anything in this list.
- **Playwright (already)** — a different language binding (e.g. Python/JS Playwright in
  a repo being migrated to Java Playwright) or an earlier/partial migration. Confirm
  which before assuming a from-scratch conversion is needed.
- **None (API-only suite)** — REST Assured (Java), `requests`/`httpx` (Python), Supertest
  (JS), RestSharp (.NET). These don't need Playwright's *browser* automation at all —
  Playwright's own `APIRequestContext` can host them, or they may not need to move at
  all if the org's Playwright push is UI-scoped. Flag this explicitly; don't force an
  API suite through a browser-migration lens.

## Step 4 — design pattern (observe, don't assume from folder names)

Open 3-5 representative page/screen classes or step-definition files and check:

- **Page Object Model, done properly** — one class per screen, locators as fields,
  actions as methods returning `this` or the next page, no direct locator use in step
  definitions/tests. (This repo's `pages/` package is this pattern.)
- **Page Object Model, done loosely** — a `pages/` folder exists but assertions or waits
  leak into it inconsistently, or step definitions still reach past it for some flows.
- **Screenplay pattern** — Actors, Tasks, Questions, Abilities as separate concepts
  (common in more mature Serenity BDD / Java setups). Structurally the furthest from a
  typical Playwright POM; budget more mapping-design time.
- **Flat / no pattern** — locators and waits directly in step definitions or test
  methods, little to no reuse. Often the *easiest* raw conversion (little abstraction to
  preserve) but the *riskiest* to trust, since there's less to cross-check behavior
  against — flag for extra scenario-by-scenario care in parity verification.

## Step 5 — the rest of the profile

For each of these, read actual code/config rather than inferring — see
`templates/framework-profile.template.md` for exactly what to record:

- **Reusable components**: shared driver lifecycle, custom wait helpers, data
  builders/factories, shared assertion utilities.
- **Config & environment management**: file-based (JSON/YAML/properties), env-var based,
  a config class, or hardcoded (flag hardcoded values per the secrets guardrail — never
  copy the value itself into your output).
- **Test data management**: fixtures, DB seeding, "register a fresh account every run,"
  shared mutable accounts (this repo does the former for its API suite and the latter,
  deliberately, for its shared UI test account — both are valid choices to document, not
  fix).
- **Locator strategy**: stable test-id attributes vs CSS vs XPath vs text, and how
  centralized vs duplicated locator definitions are.
- **Parallelization**: whether/how tests run in parallel today.
- **Reporting & CI**: report format produced, what CI system and what downstream
  consumers (dashboards, notifications, quality gates) depend on that specific format.

## When the ecosystem isn't in this matrix at all

Say so explicitly in the Framework Profile rather than forcing an unfamiliar framework
into the closest-looking row. `migration-planner` needs to know "we don't have a
documented mapping for this" as clearly as it needs a correct mapping — a false-confidence
guess costs more downstream than an honest gap.
