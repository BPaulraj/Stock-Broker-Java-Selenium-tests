# Stock Broker Java Selenium Tests

UI + REST API test automation suite for the **StockBroker Demo** app, built with Selenium 4 and REST Assured, both on Cucumber 7 (BDD) and TestNG. This is a test-only repository — there is no application source here; it drives an instance of the app (and its API) running elsewhere.

## Prerequisites

- Java 21 (JDK)
- Maven
- Google Chrome installed (driver binaries are resolved automatically at runtime by [Selenium Manager](https://www.selenium.dev/documentation/selenium_manager/) — no manual driver download needed) — only needed for the UI scenarios
- The StockBroker Demo web app running and reachable (defaults to `http://localhost:5173/`) — only needed for the UI scenarios
- The StockBroker Demo REST API running and reachable (defaults to `http://localhost:4100/api/v1`) — only needed for the `@api` scenarios

## Setup

```bash
git clone https://github.com/BPaulraj/Stock-Broker-Java-Selenium-tests.git
cd Stock-Broker-Java-Selenium-tests
```

No further setup is required — dependencies are pulled by Maven on first run.

## Configuration

Per-environment settings live in [`src/test/resources/environments.json`](src/test/resources/environments.json), one block per environment:

```json
{
  "DEV": {
    "baseUrl": "http://localhost:5173/",
    "apiBaseUrl": "http://localhost:4100/api/v1",
    "testEmail": "...",
    "testPassword": "..."
  },
  "FTA": { "baseUrl": "", "apiBaseUrl": "", "testEmail": "", "testPassword": "" },
  "INT": { "baseUrl": "", "apiBaseUrl": "", "testEmail": "", "testPassword": "" }
}
```

`FTA` and `INT` are placeholders — fill in a `baseUrl`, `apiBaseUrl`, and test account before running against those environments.

## Running the tests

Run from the repository root, with the app under test already running.

```bash
# Full suite against DEV (default environment)
mvn test

# Against a different environment (must match a key in environments.json)
mvn test -Denv=FTA

# A single scenario by name
mvn test -Dcucumber.filter.name="Successful login"

# All scenarios tagged @yourTag
mvn test -Dcucumber.filter.tags="@yourTag"

# API scenarios only (no browser/Chrome needed)
mvn test -Dcucumber.filter.tags="@api"

# UI scenarios only (no API service needed)
mvn test -Dcucumber.filter.tags="not @api"

# One-off overrides without touching environments.json
mvn test -Dbase.url=http://localhost:5173/ -Dapi.base.url=http://localhost:4100/api/v1 -Dtest.email=you@example.com -Dtest.password=secret
```

Test reports are written to `target/surefire-reports`.

## What's covered

### UI (Selenium)

15 scenarios across 8 feature files in [`src/test/resources/features`](src/test/resources/features):

| Feature | Covers |
|---|---|
| `smoke.feature` | App loads |
| `login.feature` | Successful login |
| `registration.feature` | Sign up creates and logs into a new account; sign up ↔ login navigation |
| `dashboard.feature` | Account summary, holdings, nav links, quick-link tiles |
| `trade.feature` | Company search, buying a share (debits wallet) |
| `payments.feature` | Adding funds via bank transfer (credits wallet), debit card tab |
| `inbox.feature` | Selecting a message, toggling read/unread |
| `profile.feature` | Account summary, saving profile changes |

> **Note:** `trade`, `payments`, `profile`, and `registration` scenarios perform real mutations against the demo backend (an actual buy, an actual funds transfer, an actual profile save, an actual new account) — there's no reset/seed endpoint, so the test account's wallet balance, holdings, and profile fields will drift a little with every run. That's expected, not a bug.

### API (REST Assured)

30 scenarios across 6 feature files in [`src/test/resources/features/api`](src/test/resources/features/api), all tagged `@api`, against the versioned REST API (`/api/v1/*`, JWT bearer auth):

| Feature | Covers |
|---|---|
| `auth.feature` | Register, login, duplicate-email/weak-password rejection, get/update profile, auth failures |
| `wallet_payments.feature` | Wallet balance, adding funds (bank transfer + debit card), validation failures |
| `companies_trades.feature` | Listing/searching companies, buying/selling shares, insufficient-funds/holdings rejection, auth requirement |
| `portfolio.feature` | Portfolio summary and holdings, before and after a purchase |
| `inbox.feature` | Welcome message seeded by registration, fetch by id, 404 for unknown id |
| `invoices.feature` | Invoice lookup by trade, PDF download, tenant isolation (can't fetch another user's invoice) |

Every `@api` scenario registers its own throwaway account, so these tests don't depend on (or drift) the shared UI test account. Buy/payment scenarios execute real mutations against the demo backend, same convention as the UI suite.

## Project structure

```
src/test/java/org/bharathi/
  api/              # REST Assured API clients — one class per API resource (UsersApi, TradesApi, ...)
                     # plus ApiClient (base request spec) and ApiContext (ThreadLocal scenario state)
  config/           # ConfigReader — reads environments.json, -Denv/-D overrides
  driver/           # DriverManager — ThreadLocal<WebDriver> lifecycle
  hooks/            # Cucumber @Before/@After (browser lifecycle for UI scenarios, API context reset for @api ones)
  pages/            # Page Objects — one class per screen (LoginPage, DashboardPage, ...)
  runners/          # TestRunner — Cucumber+TestNG entry point
  stepdefinitions/  # Cucumber step definitions — one class per UI feature area, plus Api*Steps for the API layer
src/test/resources/
  environments.json # Per-environment base URL, API base URL, and test credentials
  features/         # Gherkin .feature files (UI)
  features/api/     # Gherkin .feature files (REST API, all tagged @api)
testng.xml          # TestNG suite consumed by mvn test
```

See [`CLAUDE.md`](CLAUDE.md) for a deeper architectural walkthrough, including known app quirks (async data loading, locator gotchas) discovered while building this suite.
