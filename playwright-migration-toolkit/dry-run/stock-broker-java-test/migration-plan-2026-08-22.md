# Migration Plan: stock-broker-java-test — stock-broker-java-test-migration-plan-v2

Produced by: `migration-planner` | Date: 2026-08-22 | Based on Framework Profile:
`dry-run/stock-broker-java-test/framework-profile-2026-08-22.md` (repo commit `f3fac79`,
human-approved at Gate B)

**Status: Gate D approved (2026-08-22) — see "Gate D decisions" at the end of this
document. No code has been converted against this version yet; stages 3+ may now begin
with phase-1/batch-1.**

> **Supersedes**: `dry-run/stock-broker-java-test/migration-plan.md`
> (`stock-broker-java-test-migration-plan-v1`, 2026-08-22, based on the earlier
> `framework-profile.md` @ commit `82a94a4`). Per `governance/POLICY.md` §Versioning,
> that file is left in place, not overwritten. **See "Divergence from v1" at the end of
> this document** for exactly what changed and why — in short: one new finding (a
> `DashboardPage` locator inconsistency) is added to the mapping table, risk register,
> and open questions; everything else (target-stack proposal, batch list, sizing,
> cutover approach, and the three v1 open questions) is materially unchanged, because
   the underlying Framework Profile's substance didn't change, only its confidence
   detail and commit reference.

## 1. Target stack (proposal — requires Gate D approval)

- **Proposed**: Java + Playwright (this toolkit's default), keeping TestNG + Cucumber
  exactly as-is — only the browser-automation layer underneath the page objects changes
  (`WebDriver` → `Page`/`BrowserContext`). Rationale: the Framework Profile shows a
  clean, consistent Page Object Model (7 page objects, no raw `By`/`driver.findElement`
  leakage into any sampled step-definition class) — precisely the shape that ports
  close to 1:1 to Playwright's Java bindings, per the `playwright-conversion-patterns`
  skill.
- **Keep existing BDD layer?** Yes — 15 UI scenarios across 8 feature files, no
  structural complaints in the Framework Profile. Rewriting to Playwright's native
  TypeScript test syntax would bundle an unrelated Java→TypeScript language migration
  into this one for no stated benefit; the conversion-patterns skill's default is to
  keep TestNG/Cucumber for exactly this reason.
- **Alternatives considered**:
  - *TypeScript + native Playwright Test* — considered and not recommended as the
    default for this specific project. The team's entire existing investment (CLAUDE.md,
    build tooling, all 37 source files, CI knowledge if any exists externally) is
    Java/Maven/TestNG/Cucumber; the Framework Profile finds no adjacent TypeScript
    codebase or stated team preference that would offset a full language change stacked
    on top of the tooling change. This is a per-project judgment, not a restatement of
    a toolkit-wide rule — a TS-heavy team would warrant a different recommendation.
  - *Playwright's `APIRequestContext` in place of REST Assured for the `api/` layer* —
    noted here because it's the one place "Java + Playwright" doesn't automatically mean
    "convert everything to Playwright." The REST Assured suite (9 resource clients, 30
    scenarios) is not browser automation and works well as-is. See mapping-table row and
    Open Question 1 below — **not recommended by default**, flagged for explicit human
    decision rather than assumed either way.
- This section is a proposal only. Nothing below (mapping, batches, sizing) should be
  read as authorizing conversion until a human clears Gate D.

## 2. Construct mapping table

| Legacy construct | Playwright equivalent | Complexity | Rationale |
|---|---|---|---|
| `DriverManager` (`ThreadLocal<WebDriver>`, hardcoded `ChromeDriver` via Selenium Manager) | `PlaywrightContextManager` (`ThreadLocal<Page>` backed by one process-wide `Browser`, a fresh `BrowserContext`+`Page` per scenario) | Low | Same `ThreadLocal`-per-scenario shape, different payload — conversion skill's driver-lifecycle table. Also the natural point to decide whether to keep Chrome-only or add cross-browser config (currently none exists either way — not a regression to preserve Chrome-only, just worth naming) |
| `Hooks` (`@Before("not @api")`/`@After("not @api")` open/close driver; `@After("@api")` reset `ApiContext`) | Same hook shape and same tag-scoping, opens/closes a `BrowserContext` instead of a `WebDriver`; `@api` branch is unaffected since it's Cucumber-tag-level, not Selenium-level | Low | Direct port |
| 7 page objects (`private static final By` fields, `WebDriver` constructor param, fluent `this`/void/record-returning methods) | Same 7 classes, same shape, `Locator`/`Page` fields resolved lazily, `Page` constructor param | Low | 1:1 structural port per class — conversion skill's POM example. Applies to `LoginPage`, `RegisterPage`, `DashboardPage`, `TradePage`, `PaymentsPage`, `InboxPage`, `ProfilePage` |
| Per-page inline `waitFor(By)` helpers wrapping `WebDriverWait`+`presenceOfElementLocated` (`TradePage`, `InboxPage`, `PaymentsPage`) — duplicated per class, no shared base | Deleted. Playwright's locator actions and `assertThat(locator)...` web-first assertions auto-wait; "wait on a real value, not just presence" (Dashboard's async-load gotcha) becomes `assertThat(locator).hasText(...)` | Low | Same pattern repeated 3x in the legacy code collapses to zero custom wait code in Playwright — a real simplification, not just a rename |
| `DashboardPage.getHoldings()`'s inline `WebDriverWait` (the one async-sensitive read with no shared `waitFor` at all — Profile §5/Observations) | Same deletion as above; folding it into the same Playwright auto-wait pattern also removes the one place the legacy code's "no shared wait utility" gap is most visible | Low | No special handling needed once ported — Playwright doesn't have an equivalent gap to reproduce |
| `TradePage.confirmOrder()` / `PaymentsPage.addFunds()` / `ProfilePage.saveChanges()` blocking internally on an async "Processing…" → status-message transition | Same shape, using `assertThat(statusLocator).isVisible()`/`hasText(...)` instead of a hand-rolled wait loop | Low | Behavior preserved, implementation simplified |
| CSS/`href`/Tailwind-class locators, plus XPath `normalize-space()` for multi-word text labels (`TradePage`'s "Buy order"/"Sell order" panel title, per CLAUDE.md's documented convention) | Same CSS/`href`/class selectors via `page.locator(...)`; re-verify `normalize-space()` cases against `page.getByText(...)`, which handles split text nodes differently than raw XPath | Medium | Not a like-for-like simplification — no `data-testid`/ARIA-role attributes exist outside the login form, so `getByRole`/`getByTestId` aren't broadly available. See risk register |
| **New this revision** — `DashboardPage`'s three summary-value locators (`WALLET_BALANCE_VALUE`, `PORTFOLIO_VALUE`, `TOTAL_GAIN_LOSS_VALUE`) use exact-match XPath `text()='...'` on multi-word labels, *inconsistent* with the codebase's own documented `normalize-space()` convention (and with every other multi-word-label locator in the suite) | Two valid paths, **not decided here**: (a) port as-is with `page.locator("xpath=...text()='...'")` for legacy-behavior parity, since it currently works; or (b) fix during conversion to the `getByText`/`normalize-space()`-equivalent pattern used everywhere else, since the Playwright rewrite is a natural point to correct a known-fragile pattern before it bites the way "Buy order" already did once. Recommend (b) for consistency, but this is a scope decision, not a mechanical one — flagged at Open Question 4, not decided unilaterally | Low-Medium | See Framework Profile §8/Observations (new finding, not in v1's source profile). Complexity is low either way; what's undecided is *whether* to fix it, which affects whether the batch's diff is a pure port or a small behavior-preserving bug fix bundled in |
| `environments.json` + `ConfigReader` (`-Denv` selection, `-D` system-property overrides, plaintext `DEV` credentials committed in git) | Kept as-is structurally — add a `browserType`/`headless` entry if the migration wants one; do **not** switch to Playwright's `playwright.config.ts` idiom (TypeScript-project-native, not a fit for this Java suite) | Low | Per conversion skill's config guidance. The plaintext-credential handling is a separate, explicitly-flagged decision — see risk register and Open Question 3; this row is about the *mechanism* porting cleanly, not about whether the mechanism itself should change |
| No `parallel` attribute in `testng.xml` today; `ThreadLocal` infra already supports it | Unchanged for this migration (out of scope) — Playwright's `BrowserContext`-per-thread model is cheaper than Selenium's driver-per-thread model if/when parallelization is turned on later, but turning it on is not part of this plan | Low | Noted as a future opportunity, not a requirement |
| REST Assured API layer (`api/` package: `ApiClient`, 9 resource clients, `ApiContext`; 6 `@api`-tagged feature files, 30 scenarios) | **Open question — see §7.1.** Default recommendation: leave as REST Assured, out of scope for this Playwright *browser-automation* migration, since it isn't browser automation and REST Assured already serves it well with no pain points recorded in the Profile | — | Naming it here explicitly rather than silently excluding it, so the human approver sees it was considered, not overlooked |
| No CI pipeline definition found in-repo (Profile §10, confidence Medium on absence) | Out of scope by default | — | See Open Question 2 — confirm externally-defined CI doesn't exist before assuming greenfield |

## 3. Risk register

| Risk | Source (profile section / observed) | Severity | Mitigation |
|---|---|---|---|
| No stable `data-testid`/accessible-role attributes outside the login form — most Playwright locators stay CSS/`href`/text-based rather than gaining `getByRole`/`getByTestId` resilience | Profile §8 | Medium | Proceed with CSS/text locators for parity with legacy (not a regression); separately recommend the app team add `data-testid` attributes as a follow-up outside this migration's scope |
| Split-text-node label matching (`normalize-space()` XPath workaround, e.g. "Buy order"/"Sell order") may or may not need an equivalent workaround under Playwright's `getByText` matching | Profile §8, §11 | Low-Medium | Spike this specific locator during the Trade page batch (phase-2/batch-2) before assuming either outcome; budget a little extra time there |
| **New this revision** — `DashboardPage`'s `text()=` locators on multi-word labels are a latent fragility inconsistent with the codebase's own stated convention; currently passing, but the same class of bug already bit "Buy order" once | Profile §8, Observations (not present in the source profile behind v1) | Low (currently passing; no live failure) | Decide explicitly at Gate D whether phase-2/batch-1 (`DashboardPage`) ports this locator as-is or fixes it in the same batch — see Open Question 4. Do not let `migration-executor` decide this silently either way |
| Shared, drifting DEV account for most UI scenarios (`trade.feature`, `payments.feature`, `profile.feature` all mutate it for real; no reset endpoint) makes exact-value parity checks noisy — balances/holdings differ run to run | Profile §7, §11 | Medium | Use the `parity-verification` skill's behavioral-comparison approach (direction/shape of change, not exact value) for anything touching the shared account; sequence legacy-then-migrated runs per batch, never concurrent, to avoid cross-suite interference on the one shared account |
| Two UI scenarios (different ones across different runs — sign-up link lookup, mark-as-read toggle, company-list index, `#email` lookup) exhibit intermittent flakiness attributed to backend state drift, not framework defects | Profile §11 | Medium | Treat pre-existing flakiness as inherited baseline noise when triaging a batch's Parity Report, not a migration-introduced regression — but don't wave away a *new, consistently-reproducing* failure as "just flakiness" either; this is exactly the judgment call the `parity-verification` skill's noise-vs-real-gap guidance exists for |
| Plaintext DEV credentials committed in `environments.json`, tracked in git, not gitignored | Profile §6, Observations | Medium (security, not migration-blocking) | Out of scope for this migration itself by default, but flagged for the human approver since a full-suite rewrite is a natural moment to remediate it — see Open Question 3. Not fixed unilaterally |
| No CI pipeline currently wires this suite in (confidence Medium on absence — in-repo only) | Profile §10 | Low | Out of scope by default; confirm no externally-defined pipeline exists before treating this as greenfield — see Open Question 2 |
| `FTA`/`INT` environments are unpopulated placeholders — suite has only ever run against `DEV` | Profile §6, §11 | Low | No action needed for this migration; note that parity verification (stage 5/7) can only meaningfully run against `DEV` until those are populated |
| No shared base-page/wait-utility class in the legacy suite (each page reimplements its own `waitFor`, and `DashboardPage` doesn't have one at all) — a structural gap, not a bug | Profile §5, Observations | Low | Not a risk to the migration itself (Playwright's auto-waiting makes the gap moot post-conversion) but worth noting so nobody expects a shared utility class to "port" — there isn't one to port |

## 4. Phases and batches

Sequenced low-risk and foundational first, per `governance/WORKFLOW.md` and
`governance/GUARDRAILS.md` §6 (one page/screen and its dependents per batch, each
reviewable by a human in one sitting) — prove the driver-lifecycle pattern on the
smallest possible surface before touching anything with cross-page dependencies.

| Batch ID | Description | Size (S/M/L) | Depends on | Status |
|---|---|---|---|---|
| phase-1/batch-1 | `PlaywrightContextManager` (replaces `DriverManager`) + `Hooks` update + `LoginPage` + `login.feature` + `smoke.feature` | S | — | **Done** — merged [PR #1](https://github.com/BPaulraj/Stock-Broker-Java-Selenium-tests/pull/1) (`156a335`), 2026-08-22. `migration-reviewer`: Clean. `migration-verifier`: Parity Confirmed ([report](parity-reports/phase-1-batch-1.md)) |
| phase-1/batch-2 | `RegisterPage` + `registration.feature` | S | phase-1/batch-1 (uses `LoginPage.goToSignUp()`) | Not started |
| phase-2/batch-1 | `DashboardPage` + `dashboard.feature` (**includes Open Question 4's `text()=` decision — resolve before or at batch start, not mid-batch**) | M | phase-1/batch-1 | Not started |
| phase-2/batch-2 | `TradePage` + `trade.feature` (includes the split-text-node `normalize-space()`/`getByText` locator spike — see risk register) | M | phase-2/batch-1 (nav originates from Dashboard) | Not started |
| phase-2/batch-3 | `PaymentsPage` + `payments.feature` | S | phase-2/batch-1 | Not started |
| phase-2/batch-4 | `InboxPage` + `inbox.feature` | S | phase-2/batch-1 | Not started |
| phase-2/batch-5 | `ProfilePage` + `profile.feature` | S | phase-2/batch-1 | Not started |
| phase-3/batch-1 | API layer (`api/` package + `features/api/*`, REST Assured → Playwright `APIRequestContext`) — **only if Open Question 1 resolves to "yes, migrate it"; otherwise this batch does not exist** | L (if approved; would likely need splitting further into per-resource batches at plan-revision time given 9 resource clients and 30 scenarios) | Independent of phase-1/phase-2 (no shared code with the UI layer beyond `ConfigReader`) | Not started / pending decision |

Note on phase-3/batch-1 sizing: if Open Question 1 is approved, this single row is
almost certainly too large for one review sitting per `GUARDRAILS.md` §6 (9 resource
clients, 30 scenarios) — it is listed here as a placeholder only; a re-plan (bumping to
v3) would split it into per-resource batches (mirroring phase-2's per-page pattern)
before any conversion work starts against it.

## 5. Effort sizing (approximate — not a commitment)

Small suite overall: 7 UI page objects, 15 UI scenarios across 8 feature files, ~37
source files / ~1,932 lines total (Profile §12). No cross-cutting framework rewrite is
needed — the driver-lifecycle swap (`DriverManager` → `PlaywrightContextManager`) is the
only genuinely new pattern; everything else in the mapping table is a structural 1:1
port or an outright deletion (the wait-helper rows), both rated Low complexity.

- **Phase 1** (2 batches: Login/Smoke, Register) — a short, low-risk proof of the
  pattern.
- **Phase 2** (5 batches, one per remaining page) — repeats the same shape five more
  times, with two known extra-attention items: the Trade page's text-node locator spike
  (batch-2) and the Dashboard `text()=` fix-or-port decision (batch-1, pending Open
  Question 4).
- **Phase 3** (API layer) — sized only if Open Question 1 is approved; not included in
  the rough total below since it may not happen at all.

Rough order of magnitude for phases 1-2 combined: a few focused engineer-days, not
weeks — this is a sanity-check number for the human approver, not a committed estimate.
Actual pace depends more on batch review turnaround (Gate H, exercised every batch) than
on the coding itself. This figure is unchanged from v1's estimate — the new profile
didn't change scale or mapping complexity in any way that would move it.

## 6. Parallel-run & cutover approach

Legacy Selenium suite stays live and runnable throughout every batch — no batch removes,
truncates, or edits it (`governance/GUARDRAILS.md` §3); this is this project's
straightforward inheritance of the toolkit's default parallel-run policy
(`governance/POLICY.md` §Parallel-run & rollback), with one project-specific addendum:

- **Shared-account ordering**: given the shared-DEV-account drift risk above, run the
  legacy suite *before* the migrated suite in any parity check that touches it
  (`trade.feature`, `payments.feature`, `profile.feature`). `registration.feature` and
  every `@api` scenario use fresh throwaway accounts and have no such ordering
  constraint.
- **No CI-budget complication** — there's no CI pipeline currently wired in (pending
  Open Question 2's confirmation), so cutover doesn't need to coordinate around an
  existing pipeline's dual-suite run cost the way a CI-heavy project would.
- Cutover otherwise follows `governance/POLICY.md`'s standard criteria unchanged: every
  batch Done, final full-suite Parity Report shows coverage parity, joint
  engineering-owner + QA-owner sign-off, at least one clean full CI cycle (or
  equivalent manual full-suite run, given no CI currently exists) with no new
  flake/failure pattern beyond the two already-documented baseline-flaky scenarios.

## 7. Open questions for the human approver

1. **Is the REST Assured API layer in scope for this Playwright migration at all?** It
   isn't browser automation, and REST Assured already serves it well (30 passing
   scenarios, clean conventions, no pain points recorded in the Profile). Recommend:
   leave it out of scope unless there's an org-wide goal of consolidating *all* test
   tooling onto Playwright specifically, in which case Playwright's Java
   `APIRequestContext` could replace REST Assured — but that swaps a working HTTP client
   library for a different one, a materially different kind of change from the
   browser-automation conversion this plan otherwise covers, and deserves its own
   explicit yes/no rather than a default assumption either way.
2. **Should this migration also add a CI pipeline**, since none is currently wired in
   the repo (and it's worth confirming no externally-defined pipeline exists before
   treating this as greenfield — Profile §10's absence-of-CI finding is Medium
   confidence, not High, for exactly this reason)? Out of scope by default, but worth
   asking now rather than assuming — "we're touching every test file anyway" is a
   natural moment to bundle it if the org wants to, and bundling it silently without
   asking would violate this plan's own scope-discipline as much as batch scope creep
   would.
3. **Is the plaintext DEV credential committed in `environments.json` something this
   migration should also remediate** (e.g. move to an environment variable or a secrets
   manager), or is that tracked separately from the Playwright work? Recommend tracking
   it separately (pre-existing issue, not introduced or blocked by the migration, and
   remediating shared config is itself a guardrail-flagged action requiring explicit
   human go-ahead per `governance/GUARDRAILS.md` §4) — but surfacing it here so it
   doesn't silently fall through the cracks between "not this migration's problem" and
   "nobody's problem."
4. **New this revision — should `DashboardPage`'s three `text()=` summary-value
   locators be ported as-is or corrected to the `normalize-space()`/`getByText`
   convention used everywhere else in the suite, during phase-2/batch-1?** They
   currently pass (no live bug), but they're the same class of fragility that already
   caused a real bug elsewhere in this codebase ("Buy order"/"Sell order", per
   CLAUDE.md), and a full-page-object rewrite is a natural point to either fix it
   deliberately or consciously decide to carry it forward unchanged. Recommend fixing it
   in the same batch (small, well-understood, same page already being touched) but this
   is a scope decision for the approver, not something to leave to `migration-executor`'s
   judgment mid-batch.

None of the above are filler — all four are decisions this plan genuinely cannot make on
its own per `governance/GUARDRAILS.md` §7 (target-stack-adjacent tooling scope, CI scope,
secrets remediation scope, and a deliberate-fix-vs-parity-port scope call all change the
shape of the plan or the code, not just a mechanical detail).

## Divergence from v1

Compared to `stock-broker-java-test-migration-plan-v1`
(`dry-run/stock-broker-java-test/migration-plan.md`, based on the profile at commit
`82a94a4`):

- **Target stack proposal, batch list/order, sizing, and cutover approach are
  materially unchanged.** The underlying repo facts (stack, scale, POM shape, config
  mechanism, test-data strategy, absence of CI/parallelization) are the same in both
  profiles — the newer profile is a more thoroughly re-verified pass against a slightly
  newer commit (`f3fac79` vs `82a94a4`), not a discovery of new architecture.
- **One new finding, threaded through this version**: the `DashboardPage` `text()=`
  locator inconsistency (§2's new mapping-table row, §3's new risk-register row, §7's
  new Open Question 4, and the phase-2/batch-1 description note in §4). This was not
  present in the profile behind v1 and is the one place this plan's *content*, not just
  its provenance, differs from v1.
- All three of v1's open questions (API-layer scope, CI-pipeline scope,
  credential-remediation scope) are carried forward unchanged in substance — they
  weren't resolved between v1 and v2, so they remain open here as Open Questions 1-3.
- No batch was resized, reordered, or re-scoped relative to v1; no new batch was added
  beyond the conditional API-layer placeholder that v1 also had.

## Gate D decisions

Decided by: Engineering owner (via conversation, `2026-08-22`) | Approver: BPaulraj

- **Target stack**: Approved as proposed — Java + Playwright, keeping TestNG + Cucumber
  as-is. TypeScript+Playwright and the drop-Cucumber variant were considered and
  declined.
- **Open Question 1 (API layer scope)**: Declined — REST Assured `api/` layer stays out
  of scope. Phase-3/batch-1 in §4 does not proceed; this migration is UI-only.
- **Open Question 2 (CI pipeline)**: Declined — no CI pipeline is added as part of this
  migration.
- **Open Question 3 (plaintext credential remediation)**: Declined for this migration —
  tracked separately from the Playwright work, per the plan's own recommendation.
- **Open Question 4 (`DashboardPage` `text()=` locators)**: **Port as-is**, overriding
  the plan's recommendation to fix. Parity with legacy behavior takes priority over
  correcting the fragility for this migration; phase-2/batch-1 converts the three
  `WALLET_BALANCE_VALUE`/`PORTFOLIO_VALUE`/`TOTAL_GAIN_LOSS_VALUE` locators to their
  literal Playwright equivalent (e.g. `page.locator("xpath=...text()='...'")`), not to
  `normalize-space()`/`getByText`. The known fragility is not being fixed here — noted
  in case it resurfaces as a real bug in a later batch.
- **Batch sequencing**: Approved as proposed — 7-batch order in §4, low-risk-first,
  unchanged.

**Net effect on §4's batch table**: phase-3/batch-1 is dropped (API layer out of scope);
phase-2/batch-1's parenthetical note ("resolve before or at batch start") is resolved —
port as-is, no locator fix bundled into that batch. All other batches proceed as listed.

Gate D is cleared. `migration-executor` may be invoked for `phase-1/batch-1` next.

## Revision history

| Version | Date | What changed | Why |
|---|---|---|---|
| v1 | 2026-08-22 | Initial plan (dry run), based on framework-profile.md @ commit `82a94a4` | First application of the toolkit to this repo |
| v2 | 2026-08-22 | Re-planned against the independently re-verified framework-profile-2026-08-22.md @ commit `f3fac79`; added the `DashboardPage` `text()=` locator finding to the mapping table, risk register, and open questions (Open Question 4); all other content carried forward from v1 with no material change (see "Divergence from v1" above) | Newer, human-approved (Gate B) Framework Profile supersedes the one v1 was based on; re-planning against it per `governance/POLICY.md` §Versioning rather than treating v1 as still current |
