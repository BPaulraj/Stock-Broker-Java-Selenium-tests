# Migration batch PR checklist: {{batch ID}}

Paste this into the PR description for every `playwright-migration/<batch-id>` PR.

- **Migration Plan**: {{link}} — implements {{batch ID}}
- **Framework Profile**: {{link}}

## Definition of Done (governance/POLICY.md)

- [ ] Builds and passes in isolation
- [ ] `migration-reviewer` pre-review: {{Clean, date}} — findings addressed if any
- [ ] `migration-verifier` Parity Report: {{link}} — verdict: {{Parity Confirmed}}
- [ ] Scope matches the Migration Plan's batch description exactly (no drift)
- [ ] No edits to the legacy suite's files
- [ ] No CI/CD, shared config, or secrets touched
- [ ] This PR title/body references the batch ID (traceability, per Policy §Versioning)

## Human reviewer sign-off

- [ ] I reviewed the full diff, not just the checklist above
- [ ] Reviewer: {{name}} | Date: {{date}}
