# Framework Profile: {{project name}}

Produced by: `framework-analyzer` | Date: {{date}} | Repo/commit: {{repo}} @ {{commit}}

> Every section states a confidence level — High (directly observed), Medium (inferred
> from strong signals), Low (inferred from weak/indirect signals, needs human check).

## 1. Language & build tooling — confidence: {{H/M/L}}

- Language: {{}}
- Build tool: {{Maven/Gradle/npm/pip/...}}
- Key dependency versions observed: {{}}

## 2. Test runner & BDD layer — confidence: {{H/M/L}}

- Runner: {{TestNG/JUnit/pytest/...}}
- BDD layer: {{Cucumber/SpecFlow/none/...}}
- Where step definitions live relative to `.feature` files: {{}}

## 3. Browser automation library — confidence: {{H/M/L}}

- Library + version: {{}}
- API-only components (if any) and what they use instead: {{}}

## 4. Design pattern — confidence: {{H/M/L}}

- Pattern observed (POM / Screenplay / flat / other): {{}}
- Representative files sampled: {{}}
- Consistency of the pattern across the codebase: {{}}

## 5. Reusable components — confidence: {{H/M/L}}

- Driver/session lifecycle management: {{}}
- Wait/retry helpers: {{}}
- Data builders/factories: {{}}
- Shared assertion utilities: {{}}
- Other home-grown framework layer: {{}}

## 6. Config & environment management — confidence: {{H/M/L}}

- Mechanism (file/env-var/class/hardcoded): {{}}
- Environments supported: {{}}
- Secrets handling (mechanism only — never record values): {{}}

## 7. Test data management — confidence: {{H/M/L}}

- Strategy (fixtures/DB seed/fresh-account-per-run/shared account/other): {{}}
- Reset/seed endpoint or process, if any: {{}}

## 8. Locator strategy — confidence: {{H/M/L}}

- Predominant strategy: {{}}
- Stable test-id/accessibility attributes present? {{}}
- Duplication vs centralization of locators: {{}}

## 9. Parallelization — confidence: {{H/M/L}}

- Current model: {{}}
- Thread/worker count observed in config: {{}}

## 10. Reporting & CI — confidence: {{H/M/L}}

- Report format(s) produced: {{}}
- CI system: {{}}
- Downstream consumers of the report (dashboards, notifications, quality gates): {{}}

## 11. Known pain points — confidence: {{H/M/L}}

- {{From code comments, README/CLAUDE.md, TODOs, observed flakiness}}

## 12. Scale — confidence: {{H/M/L}}

- Approx. scenario/test count: {{}}
- Approx. file count / lines of test code: {{}}

## Observations

{{Anything noticed but out of scope for this profile — e.g. a bug spotted while
reading, a stale comment, a TODO worth a human's attention. Not migration
recommendations — those belong in the Migration Plan, not here.}}
