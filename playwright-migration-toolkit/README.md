# Playwright Migration Toolkit

A portable, self-contained set of AI agents, skills, and governance rules for migrating
existing UI/API test automation suites — in any language or framework — to Playwright
(Java bindings), using Claude Code's custom subagents and skills.

This folder is designed to be **copied wholesale into any repository** that needs to run
the migration. It is not wired into any one project; it is the distributable toolkit.
The `dry-run/` folder contains a worked example produced by walking this repo
(`stock-broker-java-test`, a Selenium + Cucumber + TestNG suite) through the process, as
a concrete demonstration of the outputs the agents produce.

## Why agents, not just a prompt

A single "please convert this to Playwright" prompt breaks down for three reasons this
toolkit exists to address:

1. **Different projects use different source frameworks.** Selenium/Cucumber/TestNG is
   one shape; Cypress, WebdriverIO, Robot Framework, SpecFlow, or pytest-selenium are
   others. The discovery step has to be generic, not hardcoded to one stack.
2. **Migration is not a single mechanical transform.** It requires understanding design
   patterns, reusable components, and reporting/CI integration well enough to decide
   *what* changes, *what* stays, and *in what order* — a planning step distinct from
   the mechanical conversion step.
3. **Bulk automated code changes to a test suite are risky if unsupervised.** A test
   suite is the safety net for the *application*; corrupting it silently is worse than
   not migrating at all. That demands explicit human-in-the-loop gates and hard
   guardrails, not just good intentions in a prompt.

## Structure

```
playwright-migration-toolkit/
  README.md                          # this file
  governance/
    POLICY.md                        # definition of done, roles, versioning, cutover criteria
    GUARDRAILS.md                    # hard rules + per-agent tool allowlists
    WORKFLOW.md                      # the state machine and HITL gates, with a diagram
  agents/                            # Claude Code subagent definitions (.claude/agents format)
    framework-analyzer.md            # read-only discovery -> Framework Profile
    migration-planner.md             # Framework Profile(s) -> Migration Plan
    migration-executor.md            # Migration Plan -> converted code, in a worktree, in batches
    migration-verifier.md            # runs both suites, produces a Parity Report
    migration-reviewer.md            # pre-review gate before a batch reaches a human
  skills/                            # Claude Code skill definitions (.claude/skills format)
    framework-discovery/SKILL.md     # how to identify an unknown test stack's shape
    playwright-conversion-patterns/SKILL.md   # Java+Playwright mapping rules
    migration-governance/SKILL.md    # the guardrail checklist every agent must run
    parity-verification/SKILL.md     # how to safely run legacy + migrated suites side by side
  templates/                         # output templates the agents fill in
    framework-profile.template.md
    migration-plan.template.md
    parity-report.template.md
    pr-checklist.template.md
  dry-run/
    stock-broker-java-test/
      framework-profile.md           # real output: this repo's Framework Profile
      migration-plan.md              # real output: this repo's Migration Plan to Java+Playwright
```

## Installing into a target repository

1. Copy `agents/*.md` into the target repo's `.claude/agents/`.
2. Copy `skills/*` into the target repo's `.claude/skills/`.
3. Copy `governance/` and `templates/` in wherever the team keeps process docs (they are
   referenced by name from the agents/skills, not by path, so location is flexible —
   keeping the `playwright-migration-toolkit/` folder name is the simplest option).
4. Start a **new** Claude Code session in that repo (custom agents/skills are picked up
   at session start, not hot-reloaded mid-session).
5. Follow `governance/WORKFLOW.md` — it starts with `framework-analyzer`.

## Target stack for this toolkit's conversion rules

Java + Playwright (`com.microsoft.playwright`), keeping the team's existing JVM/BDD
tooling (TestNG and/or Cucumber) where it earns its keep rather than forcing a rewrite
to Playwright's native TypeScript test runner. See
`skills/playwright-conversion-patterns/SKILL.md` for the specifics and the rationale.
This choice is a policy default, not a hard requirement — `migration-planner` records
the target stack explicitly in every Migration Plan so a future project can pick
TypeScript instead without touching agent code, only the plan.

## Human-in-the-loop, in one sentence

No agent in this toolkit merges code, deletes the legacy suite, or declares migration
complete — every one of those decisions is a named approval gate in
`governance/WORKFLOW.md`, held by a human, every time.
