---
name: parity-verification
description: How to safely run a legacy test suite and its migrated Playwright equivalent side by side without one corrupting the other's results, and how to interpret the comparison — including the specific case of suites that perform real mutating actions (real purchases, real payments) rather than mocking them. Load this before running any parity check, whether per-batch or the final full-suite check ahead of Cutover.
---

# Parity verification

## The core hazard

If both suites perform real mutating actions against the same backend/account — which
this toolkit's default philosophy favors over mocking (see this repo's own
"execute mutating actions for real" convention as a working example) — running them
concurrently, or even sequentially against the *same* shared account, can make one
suite's assertions fail because of the other suite's side effects, not because of an
actual migration defect. A false parity gap wastes a review cycle; worse, a false
parity *pass* (both suites happen to agree by coincidence of shared drifted state) is
actively misleading.

## Sequencing rules

1. **Prefer isolated identities over sequencing where the source suite supports it.**
   If the legacy or migrated suite can register its own fresh throwaway
   account/session per scenario (this repo's API suite does exactly this via
   `ApiContext`), run both against independently-created identities — no ordering
   dependency, no shared-state hazard at all. This is the strongest option and should
   be the default recommendation whenever the source Framework Profile shows the
   pattern is already in use anywhere in the suite.
2. **Where a shared account is unavoidable** (e.g. the source suite's UI tests use one
   configured test account with no self-service account creation flow, as this repo's
   UI suite does for login/dashboard/trade/payments/profile scenarios), run the legacy
   suite to completion, capture its results, *then* run the migrated suite — never
   concurrently. Note in the Parity Report that absolute values (wallet balance,
   holdings) will differ between the two runs due to state drift from the legacy run's
   own mutations; compare *behavioral* parity (did the balance decrease after a buy,
   not by what exact amount) rather than exact-value equality in this case.
3. **Never run the same mutating scenario from both suites in a way that both attempt
   to consume the same finite resource** (e.g. both suites trying to sell the last
   share of a specific holding) — sequence so the legacy run's mutation and the
   migrated run's mutation don't compete for the same state.

## What counts as a genuine parity gap vs environmental noise

| Symptom | Likely a real parity gap | Likely environmental noise |
|---|---|---|
| Migrated test fails an assertion the legacy test passed, on identical logic | Yes — investigate the conversion | — |
| Migrated test fails because of a locator that no longer matches the (real, shared, drifted) app state | — | Possibly — re-run against fresh state before concluding a gap |
| Both suites disagree on an exact numeric value (balance, count) after both performed mutations in the same run | — | Expected under shared-account sequencing (see rule 2) — check the *behavior* (direction of change), not the exact value |
| Migrated test doesn't exercise an assertion the legacy test had | Yes — this is a coverage/assertion parity gap regardless of environment, always worth flagging | — |
| A test that was already flaky in the legacy suite (documented or observed) is also flaky in the migrated suite, but with a different underlying error each run | Note it, but it's a pre-existing issue inherited, not introduced by migration — don't block the batch on it; flag it as a separate finding | — |

## Producing the comparison

For each scenario in the batch (or the whole project, for a final Cutover-readiness
check):

1. Confirm it exists in both suites (coverage parity — see `migration-governance`'s
   checklist).
2. Confirm the assertions present in the migrated version are at least as strong as the
   legacy version's (assertion parity) — read both, don't infer from names alone.
3. Run both (per the sequencing rules above) and record pass/fail for each.
4. For any mismatch, classify it using the table above before writing it up as a gap.

## Reporting

Use `templates/parity-report.template.md`. The top-line verdict
(Parity Confirmed / Parity Gaps Found, with a count) must be readable without scrolling
past the detailed comparison table — a human deciding whether to merge a batch or
approve Cutover needs that answer first, details second.
