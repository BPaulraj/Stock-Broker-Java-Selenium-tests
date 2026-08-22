---
name: playwright-conversion-patterns
description: Construct-by-construct mapping rules for converting a Selenium/WebDriver-based Java test suite (optionally with TestNG and/or Cucumber) to Java + Playwright, this toolkit's default target stack. Load this when writing or reviewing any Migration Plan's mapping table, and when migration-executor is actually converting a batch, so the whole team converges on one idiom instead of everyone improvising differently.
---

# Playwright conversion patterns (Java target)

These are defaults, not laws — a specific Migration Plan can deviate with a stated
reason (see `agents/migration-planner.md`). But absent a reason, convert this way so
every batch looks like it came from the same codebase.

## Driver / session lifecycle

| Selenium | Playwright (Java) |
|---|---|
| `WebDriver driver = new ChromeDriver();` per thread, via a `ThreadLocal<WebDriver>` holder (e.g. this repo's `DriverManager`) | `Playwright playwright = Playwright.create()` once per process; `Browser browser = playwright.chromium().launch()` once; a fresh `BrowserContext` (and `Page` from it) **per test/scenario** — contexts are cheap and give you Selenium's per-test isolation without relaunching a browser |
| `driver.quit()` in an `@After` hook | `context.close()` per test in the equivalent hook; close `browser`/`playwright` once at suite end |
| Selenium Manager auto-resolving driver binaries | Playwright's own bundled browser binaries (`playwright install`) — note this as a one-time CI setup change in the plan, not a per-test concern |

Keep the same `ThreadLocal`-per-scenario shape for parallel safety — replace what's
inside the holder (`WebDriver` → `Page`/`BrowserContext`), not the pattern itself. This
repo's `DriverManager` and `ApiContext` are exactly the shape to replicate for a
`PlaywrightContextManager` equivalent.

## Locators

| Selenium | Playwright (Java) | Notes |
|---|---|---|
| `driver.findElement(By.id("email"))` | `page.locator("#email")` or `page.getByTestId("email")` if the app has real test-id attributes | Playwright locators are lazy (re-resolved on every action) — this eliminates a whole class of Selenium staleness bugs by construction; don't port over any "re-find the element" workaround code, just delete it |
| `By.cssSelector(...)` | `page.locator(...)` (same CSS syntax) | Direct port, no behavior change |
| `By.xpath(...)` with `text()='...'` on a multi-node label | `page.locator("xpath=...")` still works, but prefer `page.getByText(..., new Locator.GetByTextOptions().setExact(false))` or `getByRole` where the app's accessibility semantics allow it — more resilient than XPath text matching, and sidesteps issues like this repo's own documented `normalize-space()` gotcha for split text nodes |
| Scoping by ancestor to disambiguate (`header a[href='/trade']` vs `main a[href='/trade']`) | `page.locator("header").locator("a[href='/trade']")` — same scoping idea, chainable locators | |

Prefer `getByRole`/`getByLabel`/`getByTestId` over raw CSS/XPath wherever the
application's markup supports it — note in the Migration Plan's risk register if the
target app (like this repo's StockBroker Demo) has few/no accessible-role or test-id
attributes outside a login form, since that means most locators stay CSS-based rather
than getting the more resilient Playwright-native locators, and flag that as a
follow-up worth raising with the app team, not something the test suite alone can fix.

## Waiting

| Selenium | Playwright (Java) |
|---|---|
| `new WebDriverWait(driver, Duration.ofSeconds(20)).until(...)` for async-loaded content (this repo's documented pattern for list-backed pages) | Delete it. Playwright's actions (`click()`, `fill()`, assertions via `assertThat(locator)...`) auto-wait for the element to be actionable/visible/attached. For "wait on a real value, not just presence" cases (this repo's Dashboard async-load gotcha), use `assertThat(locator).hasText(...)` or `page.waitForCondition`-style polling assertions, which encode the same intent Playwright-natively |
| A short custom `waitFor(...)` helper for a live-re-rendering panel (this repo's `TradePage.waitFor`) | Same idea via Playwright's built-in retrying web-first assertions (`assertThat(...).hasText(...)`, which retries until it matches or times out) — usually deletes the custom helper entirely rather than porting it |
| `Thread.sleep(...)` anywhere in legacy code (this repo's CLAUDE.md notes this was present during manual exploration and deliberately removed from real step defs) | Never port a sleep. If you find one in the legacy suite, that's a code smell worth flagging in the batch's findings even though fixing it wasn't required — it's a five-minute win once you're already in the file |

## Page Object Model shape

Selenium POM (fields = `By` locators, constructor takes `WebDriver`, methods act and
return `this` or the next page) maps close to 1:1:

```java
// Selenium
public class LoginPage {
    private static final By EMAIL_INPUT = By.id("email");
    private final WebDriver driver;
    public LoginPage(WebDriver driver) { this.driver = driver; }
    public LoginPage enterEmail(String email) {
        driver.findElement(EMAIL_INPUT).sendKeys(email);
        return this;
    }
}

// Playwright
public class LoginPage {
    private final Page page;
    public LoginPage(Page page) { this.page = page; }
    public LoginPage enterEmail(String email) {
        page.locator("#email").fill(email);
        return this;
    }
}
```

Keep locators as `Locator` fields resolved lazily (`page.locator(...)` in the
constructor, or resolved fresh per call) rather than eagerly resolved `WebElement`
fields — this is the idiomatic Playwright shape and avoids reintroducing the staleness
problems Playwright's lazy locators exist to solve.

## TestNG / Cucumber retention

This toolkit's default is to **keep** TestNG and/or Cucumber where the source suite has
them — Playwright's Java bindings integrate with both (via `playwright.junit` isn't
directly TestNG, but a `Playwright`/`Browser`/`Page` instance is just a plain object you
can manage through the existing `ThreadLocal` + `@Before`/`@After` hook pattern, exactly
as this repo does with `DriverManager`/`Hooks`). Rationale: the team's BDD/Gherkin
investment (feature files, step-definition conventions, business-readable scenarios)
and CI/reporting wiring around TestNG survive unchanged; only the browser-automation
layer underneath changes. Do not migrate to Playwright's native TypeScript test runner
under this default — that is a different, larger decision a Migration Plan must
surface explicitly if a project wants it (see `agents/migration-planner.md`).

## Parallelization

| Selenium + TestNG | Playwright (Java) |
|---|---|
| `testng.xml` `parallel="methods"`/`"classes"` with a thread count, one `WebDriver` per thread via `ThreadLocal` | Same TestNG parallel config; one `BrowserContext` (not a whole new `Browser`) per thread/test via the same `ThreadLocal` pattern — cheaper than Selenium's one-driver-process-per-thread model, so raising thread count is usually safe to revisit as a follow-up optimization, not required by the migration itself |

## Config & environments

Keep whatever the source repo's `environments.json`-equivalent pattern already is — a
resource-file-per-environment approach, `-D` system-property overrides, whatever
`framework-analyzer` found — rather than switching to Playwright's own
`playwright.config.ts` idiom, which is TypeScript-project-native and not a natural fit
for a Java suite. Add a `browserType`/`headless`/`baseUrl` section to the existing
config shape if the legacy suite didn't need one.

## Reporting & CI

Playwright (Java) doesn't produce Playwright's own HTML trace report the way the
TypeScript test runner does out of the box — trace/video/screenshot capture is still
available via the API (`context.tracing.start(...)`), but wiring it into the existing
TestNG/Cucumber HTML+JSON reporting pipeline (rather than replacing that pipeline)
is the default: preserve whatever CI step, dashboard, or notification integration
`framework-analyzer` found consuming the legacy report format, and add trace/video
artifacts alongside it rather than instead of it. Flag in the Migration Plan's risk
register if the legacy report format has a specific downstream consumer that a
default TestNG/Cucumber report from the migrated suite might not satisfy identically.
