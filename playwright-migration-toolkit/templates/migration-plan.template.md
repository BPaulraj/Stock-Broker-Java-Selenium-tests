# Migration Plan: {{project name}} — {{plan-id}}-v{{n}}

Produced by: `migration-planner` | Date: {{date}} | Based on Framework Profile: {{link, dated}}

**Status: {{Draft — awaiting Gate D approval / Approved on {{date}} by {{approver}} / Superseded by v{{n+1}}}}**

## 1. Target stack (proposal — requires Gate D approval)

- Proposed: {{Java + Playwright (toolkit default) / alternative + why}}
- Keep existing BDD layer? {{yes/no + why}}
- Alternatives considered: {{}}

## 2. Construct mapping table

| Legacy construct | Playwright equivalent | Complexity | Rationale |
|---|---|---|---|
| {{}} | {{}} | {{L/M/H}} | {{}} |

## 3. Risk register

| Risk | Source (profile section / observed) | Severity | Mitigation |
|---|---|---|---|
| {{}} | {{}} | {{L/M/H}} | {{}} |

## 4. Phases and batches

| Batch ID | Description | Size (S/M/L) | Depends on | Status |
|---|---|---|---|---|
| phase-1/batch-1 | {{}} | {{}} | — | {{Not started}} |

## 5. Effort sizing (approximate — not a commitment)

{{Total rough sizing, derived from Framework Profile scale + mapping complexity ratings}}

## 6. Parallel-run & cutover approach

- Legacy suite stays live until: {{Cutover criteria per governance/POLICY.md, or
  project-specific addendum}}
- Project-specific complications: {{CI budget / report consumers / other, or "none"}}

## 7. Open questions for the human approver

1. {{}}

{{If genuinely none, state that explicitly — do not remove this section.}}

## Revision history

| Version | Date | What changed | Why |
|---|---|---|---|
| v1 | {{}} | Initial plan | — |
