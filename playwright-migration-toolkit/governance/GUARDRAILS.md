# Guardrails

Hard rules every agent in this toolkit must follow. "Hard" means: enforced structurally
(tool allowlists, branch isolation) wherever possible, and stated as a non-negotiable
instruction everywhere structural enforcement isn't available (mainly: `Bash` is a
general-purpose tool, so an agent that has it *can* run a destructive command even if
its role shouldn't — the instruction is the only backstop there).

## 1. Tool allowlists per agent

| Agent | Read | Edit/Write | Bash | Scope |
|---|---|---|---|---|
| `framework-analyzer` | yes | **no** | read-only commands only | any repo, any branch (never checked out for editing) |
| `migration-planner` | yes | **plan artifacts only** (`migration-artifacts/` or the toolkit's own `dry-run/`), never source | read-only commands only | reads one or more Framework Profiles, never touches source |
| `migration-executor` | yes | yes, source | yes, incl. `git` | **only inside a dedicated worktree/branch**, never `main`/`master` |
| `migration-verifier` | yes | test-report artifacts only | yes, incl. running both suites | never modifies test source, only runs it and writes reports |
| `migration-reviewer` | yes | **no** | read-only commands only | reviews a diff/PR that already exists; never edits it directly |

If an agent's `.claude/agents/*.md` frontmatter can express a tool allowlist narrower
than "all tools," use it (`Edit`/`Write`/`NotebookEdit` omitted for the read-only roles).
Where it can't (Bash's breadth), the agent's own instructions carry the restriction —
treat any Bash guardrail violation caught in review as a toolkit defect to fix, not a
one-off mistake to shrug off.

## 2. Isolation

- `migration-executor` only ever works inside a dedicated git worktree on a branch named
  `playwright-migration/<phase-id>` (see `WORKFLOW.md` for phase IDs). It never commits
  to, checks out, or rebases `main`/`master`/`develop` directly.
- Legacy and migrated suites live side by side during the whole migration (e.g. Java
  Selenium tests under `src/test/java/...selenium/...` or their original location, new
  Playwright tests under a clearly separate path) — never edit legacy test files in
  place as part of "conversion"; the legacy suite is read-only reference material until
  a human explicitly retires it (see Policy §Cutover).

## 3. No silent deletion

No agent deletes, truncates, or overwrites the legacy test suite, its CI wiring, or its
reporting pipeline at any point before the human-approved Cutover gate in `WORKFLOW.md`.
If a legacy file appears genuinely obsolete mid-migration, the agent flags it in its
output for human decision — it does not act on that judgment itself.

## 4. Destructive-action confirmation

Any of the following require an explicit human go-ahead in the current turn, even if an
agent's plan already listed the action and even if a prior human approval covered the
phase in general terms:

- `git push` (including to a migration branch — pushes are visible to the team)
- `git reset --hard`, `git clean`, force-push, branch deletion
- deleting or truncating any file outside the agent's own worktree scratch space
- modifying CI/CD pipeline definitions
- modifying shared infrastructure (environments.json-equivalent config, secrets,
  service credentials)

Blanket pre-authorization ("go ahead and push whenever") is not valid for this toolkit —
scope every approval to the specific action being taken, per Claude Code's standard
execution-care policy that this toolkit inherits rather than overrides.

## 5. Secrets

Discovery and execution agents must never copy credentials, tokens, or connection
strings they encounter in legacy config into logs, plan documents, commit messages, or
new source files. Reference *how* the legacy suite manages secrets (env vars, a vault,
a config file gitignored elsewhere) in the Framework Profile; never reference the
*values*.

## 6. Batch size

`migration-executor` converts one bounded unit per batch — one page object / screen /
API resource and its dependent step definitions, not "the whole suite." A batch is sized
so a human can review its full diff in one sitting (see Policy §Definition of Done for
the review-gate size guidance). Large batches are a process smell — if the plan proposes
one, `migration-reviewer` should flag it before it starts.

## 7. Escalate, don't guess

Any agent that hits a genuine ambiguity that changes the shape of the plan or the
code — not a mechanical detail with an obvious answer — stops and asks the human rather
than picking a default and proceeding. Mechanical details (a locator's exact Playwright
selector, a variable name) are fine to decide autonomously; framework-level or
architecture-level choices (TypeScript vs Java, keep Cucumber vs drop it, how to handle
a bespoke reporting integration with no Playwright equivalent) are not.
