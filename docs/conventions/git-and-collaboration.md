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

### ADRs

ADRs will live in `docs/adr/`, created with the first decision that warrants one. Nothing beyond the KMP/CMP client choice is settled yet, so the directory does not exist.
