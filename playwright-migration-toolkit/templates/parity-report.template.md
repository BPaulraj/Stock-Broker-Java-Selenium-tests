# Parity Report: {{project name}} — {{batch ID or "full suite"}}

Produced by: `migration-verifier` | Date: {{date}} | Legacy run: {{timestamp}} | Migrated run: {{timestamp}}

## Verdict: {{PARITY CONFIRMED / PARITY GAPS FOUND (n)}}

{{One or two sentences — the human reading this should not need to read further to
know whether this batch is mergeable / whether Cutover is ready.}}

## Scope

- Batch/scenarios covered: {{}}
- Sequencing used (concurrent / sequential / isolated accounts): {{}} — see
  `skills/parity-verification/SKILL.md` for why this matters

## Scenario-by-scenario comparison

| Scenario | Legacy result | Migrated result | Coverage parity | Assertion parity | Outcome parity | Notes |
|---|---|---|---|---|---|---|
| {{}} | {{pass/fail}} | {{pass/fail}} | {{yes/no}} | {{yes/no/weaker}} | {{yes/no}} | {{}} |

## Gaps found (if any)

| # | Scenario | Gap type (coverage/assertion/outcome/side-effect) | Description | Classified as (real gap / environmental noise, per skill's table) | Recommended next step |
|---|---|---|---|---|---|
| 1 | {{}} | {{}} | {{}} | {{}} | {{Send back to migration-executor / other}} |

## Notes on environmental factors

{{Anything relevant to interpreting the above — shared account drift, known
pre-existing flakiness inherited from the legacy suite, etc.}}
