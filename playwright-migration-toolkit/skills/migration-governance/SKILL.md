---
name: migration-governance
description: The operational self-check checklist every agent in this toolkit runs before reporting a stage done — a procedural distillation of governance/GUARDRAILS.md and governance/POLICY.md. Load this at the start of any migration-executor, migration-reviewer, or migration-verifier invocation, and whenever an agent is unsure whether an action needs human confirmation first.
---

# Migration governance checklist

This skill exists so the rules in `governance/GUARDRAILS.md` and
`governance/POLICY.md` are something an agent actually *runs through*, not just
background reading. If you're an agent in this toolkit, work through the section
matching your role before you report your stage done.

## Before doing anything destructive or state-changing (all agents)

- [ ] Is this action on the guardrail list requiring explicit per-action human
      confirmation (`git push`, force-push, `reset --hard`, `clean`, deleting/truncating
      a file outside your own worktree scratch space, touching CI/CD config, touching
      shared secrets/config)? If yes: stop and ask, even if a prior approval covered the
      phase in general terms.
- [ ] Am I about to edit a legacy test file "in place"? If yes: stop — legacy suite
      files are read-only reference material until Cutover, full stop, no exceptions for
      "just a cleanup."
- [ ] Am I about to copy a credential/token/connection-string *value* (not just
      describe the mechanism) into an output document, log, or new source file? If yes:
      stop and redact — describe the mechanism, never the value.

## `migration-executor`, before reporting a batch done

- [ ] Working inside a dedicated worktree on `playwright-migration/<batch-id>`, never
      on `main`/`master`/`develop`.
- [ ] Diff is scoped to exactly this batch's stated scope in the Migration Plan — no
      drift into another batch's files, no unrelated cleanup bundled in.
- [ ] No `git push` has happened.
- [ ] New tests build and pass locally, in isolation, before handoff.
- [ ] Any ambiguity that was architectural (not mechanical) got surfaced to a human
      instead of resolved by picking a default — see `governance/GUARDRAILS.md` §7 for
      the distinction.

## `migration-reviewer`, before reporting a verdict

- [ ] Checked scope adherence against the Migration Plan's batch description.
- [ ] Checked convention adherence against `playwright-conversion-patterns`.
- [ ] Checked for guardrail violations in the diff itself (legacy file edits, secrets,
      CI/config changes, evidence of a push beyond the local branch).
- [ ] Verdict is honest — "no findings" only if there genuinely are none, not softened
      to avoid friction, and not padded with invented nitpicks to look thorough either.

## `migration-verifier`, before reporting a Parity Report

- [ ] Both suites were run in a way that didn't let one corrupt the other's shared
      state (see `parity-verification` skill).
- [ ] Coverage parity checked (nothing silently dropped), not just outcome parity
      (both green).
- [ ] Assertion strength compared, not just pass/fail — a migrated test that passes by
      asserting less than the legacy one is a gap, not a pass.
- [ ] Top-line verdict is unambiguous: Parity Confirmed, or Parity Gaps Found (with a
      count) — never a report a human has to fully read just to know which one it is.

## `migration-planner`, before reporting a plan done

- [ ] Target stack recorded as a *proposal* for Gate D approval, not asserted as
      decided.
- [ ] Every batch sized so a human can review its full diff in one sitting (see
      `governance/GUARDRAILS.md` §6) — if a batch looks too big, split it before
      finalizing, don't leave it for `migration-executor` to discover mid-conversion.
- [ ] Open questions section is honest — real open questions listed, not omitted to
      look more finished than the plan actually is, and not padded with filler ones.

## `framework-analyzer`, before reporting a profile done

- [ ] Every section has a stated confidence level (High/Medium/Low); nothing presented
      as certain that was actually inferred rather than directly observed.
- [ ] No opinion about *how* to migrate anywhere in the output — that's
      `migration-planner`'s job; a Framework Profile that already argues for a
      migration approach is doing someone else's job and should be trimmed back to
      description only.

## Anyone, at any point

- [ ] If the honest answer to "would a human be surprised by what I'm about to do" is
      yes, stop and say what you're about to do before doing it. This is the toolkit's
      actual backstop where a specific rule doesn't quite cover the situation.
