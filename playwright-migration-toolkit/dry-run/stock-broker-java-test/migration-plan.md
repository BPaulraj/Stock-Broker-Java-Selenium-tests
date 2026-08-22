# Migration Plan: stock-broker-java-test — stock-broker-java-test-migration-plan-v1

Produced by: `migration-planner` (dry run — see note at end of `framework-profile.md`
in this same folder for how this was produced) | Date: 2026-08-22 | Based on Framework
Profile: `dry-run/stock-broker-java-test/framework-profile.md`, dated 2026-08-22

**Status: Draft — awaiting Gate D approval (this is a dry run; nobody has approved this
plan, and no code has been converted against it)**

## 1. Target stack (proposal — requires Gate D approval)

- **Proposed**: Java + Playwright (this toolkit's default), keeping TestNG + Cucumber
  exactly as-is — only the browser-automation layer underneath the page objects changes
  (`WebDriver` → `Page`/`BrowserContext`). Rationale: the Framework Profile shows a
  clean, consistent Page Object Model with no direct Selenium leakage into step
  definitions, which is precisely the shape that ports cleanly to Playwright's Java
  bindings with a 1:1 class structure — see `playwright-conversion-patterns` skill.
- **Keep existing BDD layer?** Yes — 15 well-organized Cucumber scenarios with no
  structural complaints in the Framework Profile; rewriting them to Playwright's native
  TypeScript test syntax would be a second, unrelated migration (Java→TypeScript) bundled
  into this one for no stated benefit.
- **Alternatives considered**: TypeScript + native Playwright Test was considered and
  rejected as the default for *this* project specifically — the team's existing
  investment is entirely Java/TestNG/Cucumber (see Framework Profile §1-2), and there's
  no signal in the profile (no adjacent TS codebase, no stated team preference) that
  would offset the cost of a full language change on top of the tooling change. This
  is a per-project judgment, not a restatement of the toolkit-wide default — a future
  project with a TS-heavy team should get a different recommendation here.

## 2. Construct mapping table

| Legacy construct | Playwright equivalent | Complexity | Rationale |
|---|---|---|---|
| `DriverManager` (`ThreadLocal<WebDriver>`, `ChromeDriver` via Selenium Manager) | `PlaywrightContextManager` (`ThreadLocal<Page>` backed by a shared `Browser`, one `BrowserContext`+`Page` per scenario) | Low | Same shape, different payload — see conversion skill's driver-lifecycle table |
| `Hooks` (`@Before`/`@After` open/close driver) | Same hook shape, opens/closes context instead of driver; `@api`-tag skip logic (added this session) carries over unchanged since it's Cucumber-level, not Selenium-level | Low | |
| 7 page objects (`By` fields + `WebDriver` constructor param) | Same 7 classes, `Locator`/`Page` fields, `Page` constructor param, same method shape (return `this`/next page) | Low | 1:1 structural port per class — see conversion skill's POM example |
| `WebDriverWait` in `TradePage.waitFor(...)` (live-re-rendering panel) | Playwright web-first assertions (`assertThat(locator).hasText(...)`) or `page.waitForCondition` | Low | Deletes custom code rather than porting it — Playwright's retrying assertions cover this natively |
| `presenceOfElementLocated` waits inline in list-reading methods (`DashboardPage.getHoldings()`, `TradePage.getCompanies()`, `PaymentsPage.getTransactionHistory()`, `InboxPage.getMessages()`) | Deleted — Playwright's locator actions auto-wait; "wait on a real value, not just presence" becomes a web-first assertion (`assertThat(locator).hasText(...)`) | Low | Same pattern as above, repeated across 4 page objects |
| `TradePage.confirmOrder()`/`PaymentsPage.addFunds()`/`ProfilePage.saveChanges()` blocking internally until a status message appears | Same shape, using a web-first assertion instead of a hand-rolled wait loop | Low | Behavior preserved, implementation simplified |
| CSS/text/`href`-based locators (no stable test-id attributes outside login) | Same CSS/text/`href` selectors via `page.locator(...)`; `getByRole`/`getByTestId` not broadly usable given the app's current markup | Medium | Not a like-for-like simplification — flagged in risk register below |
| `normalize-space()` XPath workaround for split-text-node labels ("Buy order"/"Sell order") | Re-verify against Playwright's `getByText`, which normalizes across descendant text nodes differently than raw XPath `text()=` — may simplify, may need the same XPath escape hatch | Low-Medium | Needs a spike during batch conversion, not assumed either way — see risk register |
| `environments.json` + `ConfigReader` (`-Denv`, `-D` overrides) | Kept as-is, unchanged — add a `browserType`/`headless` entry if needed, don't switch to `playwright.config.ts` (that's TS-project-native, not a fit here) | Low | Per conversion skill's config guidance |
| No `parallel` in `testng.xml` today | Unchanged for this migration (out of scope) — `ThreadLocal` pattern in both `DriverManager` and its Playwright replacement makes enabling it later a follow-up, not a blocker | Low | Not part of this plan's scope; noted as a future opportunity |
| REST Assured API layer (`api/` package, 30 scenarios) | **Open question — see §7.** Default recommendation: leave as REST Assured, out of scope for this Playwright migration, since it's not browser automation and REST Assured already serves it well | — | See risk register and open questions |

## 3. Risk register

| Risk | Source | Severity | Mitigation |
|---|---|---|---|
| No stable test-id/accessibility attributes outside the login form means most Playwright locators stay CSS/text-based rather than gaining the resilience `getByRole`/`getByTestId` would offer | Framework Profile §8 | Medium | Proceed with CSS/text locators for now (parity with legacy, not a regression); separately recommend the app team add `data-testid` attributes as a follow-up outside this migration's scope |
| Split-text-node label matching (`normalize-space()` workaround) may or may not need an equivalent workaround under Playwright's text matching | Framework Profile §8, §11 | Low-Medium | Spike this specific locator during the Trade page batch (phase-2/batch-2) before assuming either outcome; budget a little extra time there |
| Shared, drifting DEV account for most UI scenarios makes exact-value parity checks noisy (balances/holdings differ run to run) | Framework Profile §7, §11 | Medium | Use the `parity-verification` skill's behavioral-comparison approach (direction of change, not exact value) for anything touching the shared account; sequence legacy-then-migrated runs, never concurrent |
| Two different UI scenarios were observed flaky across two full-suite runs this session, with different failures each time | Framework Profile §11 | Medium | Treat pre-existing flakiness as inherited, not migration-introduced, when triaging parity gaps — but don't wave away a *new* consistent failure by assuming it's "just flakiness" either; the `parity-verification` skill's noise-vs-real-gap table exists for exactly this judgment call |
| Plaintext DEV credentials committed in `environments.json` | Framework Profile §6 | Medium (security, not migration-blocking) | Out of scope for this migration itself, but flagged for the human approver — worth a separate remediation regardless of Playwright timing |
| No CI pipeline currently wires this suite in | Framework Profile §10 | Low | Out of scope for this plan by default; note as a natural moment to add CI if the org wants to, as a separate decision at Gate D |

## 4. Phases and batches

Sequenced low-risk and foundational first, per `governance/WORKFLOW.md`'s guidance —
prove the driver-lifecycle pattern on the smallest possible surface before touching
anything with cross-page dependencies.

| Batch ID | Description | Size | Depends on | Status |
|---|---|---|---|---|
| phase-1/batch-1 | `PlaywrightContextManager` (replace `DriverManager`) + `Hooks` update + `LoginPage` + `login.feature` + `smoke.feature` | S | — | Not started |
| phase-1/batch-2 | `RegisterPage` + `registration.feature` | S | phase-1/batch-1 (`LoginPage.goToSignUp()`) | Not started |
| phase-2/batch-1 | `DashboardPage` + `dashboard.feature` | M | phase-1/batch-1 | Not started |
| phase-2/batch-2 | `TradePage` + `trade.feature` (includes the split-text-node locator spike — see risk register) | M | phase-2/batch-1 (nav from Dashboard) | Not started |
| phase-2/batch-3 | `PaymentsPage` + `payments.feature` | S | phase-2/batch-1 | Not started |
| phase-2/batch-4 | `InboxPage` + `inbox.feature` | S | phase-2/batch-1 | Not started |
| phase-2/batch-5 | `ProfilePage` + `profile.feature` | S | phase-2/batch-1 | Not started |
| phase-3/batch-1 | API layer (`api/` package + `features/api/`) — **only if §7's open question resolves to "yes, migrate it"** | L (if approved) | independent of phase-1/2 | Not started / pending decision |

## 5. Effort sizing (approximate — not a commitment)

Small suite overall: 7 page objects, 15 UI scenarios, no cross-cutting framework
rewrite needed (the driver-lifecycle swap is the only genuinely new pattern; everything
else is a structural 1:1 port per the mapping table's mostly-Low complexity ratings).
Phase 1 (2 batches) is a short, low-risk proof of the pattern. Phase 2 (5 batches)
follows the same shape 5 more times, with one spike (Trade page's text-node locator).
Rough order of magnitude for phases 1-2 combined: a few focused engineer-days, not
weeks — but treat this as a sanity-check number for the human approver, not a
committed estimate; actual pace depends on batch review turnaround (Gate H, every time)
more than on the coding itself.

## 6. Parallel-run & cutover approach

Legacy Selenium suite stays live and runnable throughout — no batch removes or edits it
(`governance/GUARDRAILS.md` §3). Given the shared-account drift risk above, recommend
running the legacy suite *before* the migrated suite in any parity check involving the
shared DEV account (registration's throwaway-account scenarios have no such
constraint and can run in either order safely). No project-specific CI-budget
complication identified, since there's no CI pipeline currently — cutover doesn't need
to coordinate around an existing pipeline's dual-suite cost. Cutover otherwise follows
`governance/POLICY.md`'s standard criteria unchanged.

## 7. Open questions for the human approver

1. **Is the REST Assured API layer in scope for this Playwright migration at all?**
   It isn't browser automation, and REST Assured already serves it well (30 passing
   scenarios, added this same session, following clean conventions). Recommend: leave
   it out of scope unless there's an org-wide goal of consolidating *all* test tooling
   (UI and API) onto Playwright specifically, in which case Playwright's Java
   `APIRequestContext` could replace REST Assured — but that's a materially different
   kind of change (swapping a working HTTP client library, not a browser-automation
   library) and deserves its own explicit yes/no, not a default assumption either way.
2. **Should this migration also add a CI pipeline**, since none currently exists? Out
   of scope by default (see risk register), but worth asking now rather than assuming,
   since "we're touching every test file anyway" is a natural moment to bundle it if
   the org wants to — bundling it silently without asking would violate this plan's own
   scope-discipline principle just as much as executor scope creep would.
3. **Is the plaintext DEV credential in `environments.json` something this migration
   should also remediate** (e.g. move to an env-var/secret-manager pattern), or is that
   tracked separately? Recommend tracking separately (it's a pre-existing issue, not
   something the migration introduces or is blocked by) but flagging it here so it
   doesn't silently fall through the cracks between "not this migration's problem" and
   "nobody's problem."

## Revision history

| Version | Date | What changed | Why |
|---|---|---|---|
| v1 | 2026-08-22 | Initial plan (dry run) | First application of the toolkit to this repo |
