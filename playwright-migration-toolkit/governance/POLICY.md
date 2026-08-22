# Policy

## Roles (RACI)

| Decision | Agent recommends | Human approves |
|---|---|---|
| Framework Profile is accurate | `framework-analyzer` | Tech lead / SME for that project |
| Migration Plan (target stack, phasing, risk) | `migration-planner` | Engineering owner (org-level, since target stack is a standing decision — see §Target stack below) |
| A batch's converted code is correct | `migration-executor` + `migration-reviewer` (pre-review) | Reviewer with merge rights (normal PR review) |
| Parity between legacy and migrated suite | `migration-verifier` | QA/test owner for that project |
| Retiring the legacy suite (Cutover) | — (no agent recommends this) | Engineering owner + QA/test owner, jointly |

No agent is Accountable for anything in this table. Agents are Responsible for
producing the recommendation; a named human is always Accountable for accepting it.

## Definition of Done, per batch

A converted batch is done when all of the following hold:

1. It compiles/builds and its own tests pass in isolation.
2. `migration-verifier` has produced a Parity Report for that batch showing behavioral
   equivalence with the legacy version (same scenarios covered, same pass/fail outcomes
   on a clean run, no silently-dropped assertions).
3. `migration-reviewer` has checked it against `migration-plan.md`'s stated scope for
   that batch and the `playwright-conversion-patterns` skill, with no unresolved findings.
4. A human with merge rights has reviewed and approved the PR.
5. The PR body references the Migration Plan phase/batch ID it implements (traceability
   — see below).

A batch that can't be reviewed by a human "in one sitting" is too big; split it. This is
a process guardrail as much as a governance one — see `GUARDRAILS.md` §6.

## Definition of Done, for the whole migration

The migration for a project is done when every batch in its Migration Plan meets the
above, `migration-verifier` has produced a final full-suite Parity Report, and the
Cutover gate (below) has been exercised.

## Versioning & traceability

- Every Migration Plan gets an ID (`<project>-migration-plan-v<n>`) and a phase/batch
  numbering scheme the executor and reviewer reference by ID, not by informal
  description. Re-plans (scope changes mid-migration) bump `v<n>` and note what changed
  from the prior version and why.
- Every migration PR title/body references the batch ID it implements
  (e.g. `[phase-2/batch-3] Convert TradePage to Playwright`).
- Framework Profiles and Migration Plans are kept as committed artifacts (see
  `dry-run/` for the pattern) so the reasoning behind migration decisions outlives the
  chat session that produced them.

## Parallel-run & rollback

The legacy suite is never deleted as a side effect of migration work. It keeps running
in CI (or is trivially re-enabled) until Cutover. This is the rollback mechanism: if a
migrated batch turns out to be wrong after merge, the legacy equivalent is still there
to fall back on while the migrated version is fixed — no `git revert` archaeology needed
under pressure.

## Cutover criteria

The legacy suite for a project may be retired (removed from CI, then deleted in a
follow-up PR) only when **all** of:

- Every batch in the (final version of the) Migration Plan is Done per above.
- `migration-verifier`'s final Parity Report shows full scenario-coverage parity.
- The Cutover has explicit sign-off from both the engineering owner and the QA/test
  owner for that project, recorded in the PR that removes the legacy suite (not just
  said verbally).
- At least one full CI cycle has run the migrated suite as the suite-of-record with no
  new flake/failure pattern that wasn't already present in the legacy suite.

## Target stack (standing decision)

Java + Playwright is this toolkit's default target (see `README.md`). Changing this
default for a *specific* project is a Migration Plan decision the engineering owner
approves at Plan-approval time, per the roles table above — it is not something
`migration-planner` decides unilaterally, since it affects hiring, onboarding, and
tooling investment beyond the one migration.

## Toolkit change control

Changes to `agents/`, `skills/`, or this `governance/` folder itself are treated like
changes to a shared library: proposed via PR, reviewed by whoever owns the migration
program, and versioned so an in-flight project's Migration Plan can note which toolkit
version it was planned against.
