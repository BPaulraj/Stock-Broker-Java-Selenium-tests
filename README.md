# Stock Broker Java Selenium Tests

UI test automation suite for the **StockBroker Demo** web app, built with Selenium 4, Cucumber 7 (BDD), and TestNG. This is a test-only repository — there is no application source here; it drives an instance of the app running elsewhere.

## Prerequisites

- Java 21 (JDK)
- Maven
- Google Chrome installed (driver binaries are resolved automatically at runtime by [Selenium Manager](https://www.selenium.dev/documentation/selenium_manager/) — no manual driver download needed)
- The StockBroker Demo app running and reachable (defaults to `http://localhost:5173/`)

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
    "testEmail": "...",
    "testPassword": "..."
  },
  "FTA": { "baseUrl": "", "testEmail": "", "testPassword": "" },
  "INT": { "baseUrl": "", "testEmail": "", "testPassword": "" }
}
```

`FTA` and `INT` are placeholders — fill in a `baseUrl` and test account before running against those environments.

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

# One-off overrides without touching environments.json
mvn test -Dbase.url=http://localhost:5173/ -Dtest.email=you@example.com -Dtest.password=secret
```

Test reports are written to `target/surefire-reports`.

## What's covered

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

## Project structure

```
src/test/java/org/bharathi/
  config/           # ConfigReader — reads environments.json, -Denv/-D overrides
  driver/           # DriverManager — ThreadLocal<WebDriver> lifecycle
  hooks/            # Cucumber @Before/@After (open/close the browser per scenario)
  pages/            # Page Objects — one class per screen (LoginPage, DashboardPage, ...)
  runners/          # TestRunner — Cucumber+TestNG entry point
  stepdefinitions/  # Cucumber step definitions, one class per feature area
src/test/resources/
  environments.json # Per-environment base URL + test credentials
  features/         # Gherkin .feature files
testng.xml          # TestNG suite consumed by mvn test
```

See [`CLAUDE.md`](CLAUDE.md) for a deeper architectural walkthrough, including known app quirks (async data loading, locator gotchas) discovered while building this suite.
