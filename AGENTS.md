# Family Planner — Contributor Guide

Central reference for all contributors (human or AI). For the project pitch, repository structure and tech stack, see [`README.md`](README.md).

## 1. Product Context

Family Planner reduces the mental load of running daily life: meal planning, shared events and shared tasks, kept in sync.

- **The unit is a group**, not a couple. A group can be a single person, two adults, or a whole family. Never assume a fixed number of members.
- **The core value is synchronization** — between a member's devices, and between the members of the group. Anything that breaks silently when two devices edit at once is a bug, not an edge case.
- **One group is supported today.** Multi-group is not a V1 feature, but no data-model decision may close the door on it. When modeling, keep group scoping in mind even where it is not yet used.
- **Low cognitive load is a product requirement, not a nice-to-have.** Decisions are made in advance, instructions are explicit, screens stay simple. The app has to be usable when attention is scarce.

Feature scope and phases live in [`docs/roadmap.md`](docs/roadmap.md). Agreed product behaviour lives in [`docs/prd/`](docs/prd/) — [PRD-001](docs/prd/prd-001-group-and-synchronization.md) defines the group, what is shared inside it, and what the app guarantees when several devices change the same thing. Everything else builds on it.

> [!IMPORTANT]
> [`docs/draft-spec.md`](docs/draft-spec.md) is a **draft**, not a specification. Only the KMP/CMP client is a settled decision. Do not treat the data model, the screen list or the notification design in that document as agreed — they are superseded by the PRDs under [`docs/prd/`](docs/prd/) as each one is written. Where a PRD and the draft disagree, the PRD wins.

## 2. Project Guidelines and Conventions

The conventions live in the shared [`cyrillrx/coding-conventions`](https://github.com/cyrillrx/coding-conventions) repository — the single source of truth. The documents below are thin pointers to it; some add project-specific bindings (marked _+ project_). Do not duplicate the shared rules here.

### Collaboration and Communication

- **Collaboration, Git & CI Conventions**: [`git-and-collaboration.md`](docs/conventions/git-and-collaboration.md) _(+ project)_
- **Documentation Conventions**: [`docs-conventions.md`](docs/conventions/docs-conventions.md) _(pointer)_
- **Documentation language**: All documentation, comments, commit messages, and PR descriptions must be written in English.
- **AI Co-authorship**: Do not add AI co-author tags (e.g. `Co-Authored-By: Claude`) or generated-by footers to commits or pull requests.
- **AI/Agent rules**: All rules applying to AI agents must be written in this file (`AGENTS.md`). Agent-specific config files (e.g. `.claude/CLAUDE.md`) must only point to this file — never duplicate or extend rules there.
- **AI/Agent naming convention**: Commits and PRs related to AI agent configuration or rules must use the `docs(agents)` conventional-commit prefix (e.g. `docs(agents): add co-authorship rule`).

### Code Quality and Maintainability

- **General Coding Conventions**: [`coding-conventions.md`](docs/conventions/coding-conventions.md) _(pointer)_

### Technology-Specific Guidelines

- **Client Application (KMP/Compose Multiplatform) Conventions**:
    - [`kmp-conventions.md`](docs/conventions/kmp-conventions.md) _(+ project)_

## 3. Repository Structure

This is a monorepo. Each top-level component owns its own build and its own CI workflow.

| Path       | Component                                                                                      | Status              |
|------------|------------------------------------------------------------------------------------------------|---------------------|
| `cmp-app/` | KMP/CMP client (iOS + Android)                                                                 | Not yet initialized |
| `server/`  | Server-side service — notifications, invitation redemption, outbound API calls, secret custody | Not yet initialized |
| `docs/`    | Roadmap, drafts, convention pointers                                                           | —                   |

Inside `cmp-app/`, the client splits into two shared modules and three platform wrappers — `shared/core` (domain and data, no Compose), `shared/ui` (Compose UI), `androidApp`, `desktopApp` and `iosApp`. See [ADR-001](docs/adr/adr-001-kmp-client-targets.md).

All three targets are shipped, with **Desktop ranked second** behind iOS and Android ([ADR-002](docs/adr/adr-002-desktop-product-surface.md)). Desktop is also the target that runs the tests and produces coverage. Two rules follow: every client dependency must exist on JVM, and a Desktop-only regression does not block a release — which is not licence to leave it broken.

Rules that follow from the layout:

- A change touching a single component stays inside that component's directory, and its commit scope names that component (see [`git-and-collaboration.md`](docs/conventions/git-and-collaboration.md)).
- CI workflows are filtered by path, one per component. Adding a component means adding its workflow.
- Nothing shared between components lives at the root beyond documentation and tooling config.

## 4. Commands

All commands run from `cmp-app/`:

```bash
./gradlew build                # Build every target
./gradlew jvmTest              # Run the JVM tests
./gradlew koverXmlReportJvm    # Generate the coverage reports SonarCloud reads
./gradlew ktlintCheck          # Check formatting
./gradlew ktlintFormat         # Auto-fix formatting
./gradlew :desktopApp:run      # Run on Desktop
./gradlew :androidApp:installDebug   # Install on Android
```

ktlint is **strict** in `shared/core` (`ignoreFailures = false`) and permissive in `shared/ui` (`ignoreFailures = true`).

The iOS application is built from `cmp-app/iosApp/iosApp.xcodeproj` in Xcode. Its build phase calls `./gradlew :shared:ui:embedAndSignAppleFrameworkForXcode`, so the framework is produced by Gradle and embedded by Xcode.

### Coverage policy

Coverage comes from `jvmTest` alone and reaches SonarCloud through Kover. Composables, theme tokens and route declarations are excluded: no Compose UI test feeds Kover, so measuring them would count tests that are never collected.

**An exclusion has to be declared on both sides.** Kover decides what lands in the report; Sonar indexes the sources either way and reads a file's absence from the report as zero coverage. Anything excluded in `kover {}` needs its counterpart in `sonar.coverage.exclusions` — the two use different vocabularies, Kover matching class names and Sonar matching file paths.

The practical rule when writing code: **testable logic belongs in `shared/core`**. Kover counts per class and every top-level declaration in a file compiles into a single facade, so a pure function sharing a file with a composable is excluded along with it. See [ADR-001](docs/adr/adr-001-kmp-client-targets.md).

## 5. Shared Tooling

The Claude Code plugins declared in [`.claude/settings.json`](.claude/settings.json) come from the `cyrillrx-conventions` marketplace and install on folder trust:

| Plugin            | Provides                                         |
|-------------------|--------------------------------------------------|
| `git-workflow`    | `/commit`, `/triage-findings`, `/address-review` |
| `kmp-conventions` | `kmp-style` (auto-invoked)                       |

The plugin skills are derived from the convention documents. When a rule and a skill disagree, the document in `cyrillrx/coding-conventions` wins — report the drift there rather than working around it here.
