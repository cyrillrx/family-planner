# Git & Collaboration Conventions

> [!IMPORTANT]
> **Canonical source of truth** (shared, project-agnostic): [`collaboration/git-and-collaboration.md`](https://github.com/cyrillrx/coding-conventions/blob/main/collaboration/git-and-collaboration.md) — do not duplicate here.

Conventional Commits, trunk-based branching, atomic commits, PR etiquette, the authorship rule, ADR guidance, and the [code review emoji legend](https://github.com/cyrillrx/coding-conventions/blob/main/collaboration/code-review-emojis.md) all live in the canonical document. Only the project-specific bindings below differ.

## Project-specific additions

### Commit scopes

Use short, consistent scopes matching this repository's structure:

| Scope                     | Covers                                                     |
|---------------------------|------------------------------------------------------------|
| `project`                 | Root-level tooling, README, repository-wide changes        |
| `agents`                  | AI agent configuration and rules (`AGENTS.md`, `.claude/`) |
| `cmp-app`                 | The KMP/CMP client                                         |
| `telegram-bot`            | The notification scripts                                   |
| `docs`                    | Documentation that is not tied to a single component       |
| `meal`, `events`, `tasks` | Feature-scoped changes, once features exist                |

### CI pipeline

CI is split per component under [`.github/workflows/`](../../.github/workflows/), each workflow filtered by path. Today that is [`ci-kmp.yml`](../../.github/workflows/ci-kmp.yml) for the `cmp-app/` client. Adding a component means adding its workflow. The relevant checks must pass for a PR to be mergeable.

### ADRs and PRDs

Both live under `docs/`, numbered in order, and are written from the templates in the canonical repository: [`templates/adr-template.md`](https://github.com/cyrillrx/coding-conventions/blob/main/templates/adr-template.md) and [`templates/prd-template.md`](https://github.com/cyrillrx/coding-conventions/blob/main/templates/prd-template.md).

| Directory   | Holds                                        | First document                                                          |
|-------------|----------------------------------------------|-------------------------------------------------------------------------|
| `docs/adr/` | Technical decisions and their reasoning      | [ADR-001](../adr/adr-001-kmp-client-targets.md) — client targets        |
| `docs/prd/` | Product behaviour, from the user's viewpoint | [PRD-001](../prd/prd-001-group-and-synchronization.md) — group and sync |

A PRD states what the product does; the ADR that follows it states how. When an ADR settles a question a PRD left open, link it from the PRD rather than deleting the question.
