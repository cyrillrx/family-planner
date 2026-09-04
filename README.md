# family-planner

KMP/CMP app for managing daily family life — iOS and Android first, Desktop second. Reduces mental load by centralizing meal planning, shared events and shared tasks.

## Context

The app serves **one group** — a single person, two adults, or a whole family. What it has to get right is **synchronization**: between a member's devices, and between the members of the group.

Low cognitive load is a hard requirement, not a nice-to-have: decisions are made in advance, instructions are explicit, screens stay simple. The app has to be usable when attention is scarce.

A single group is supported for now. Multi-group is not planned for V1, but no data-model decision should close the door on it.

## Repository structure

Monorepo. Each component owns its own build and its own CI workflow.

```
family-planner/
├── cmp-app/            # KMP/CMP client (Android, iOS, Desktop)
│   ├── shared/core/    # domain and data — no Compose
│   ├── shared/ui/      # Compose UI
│   ├── androidApp/  desktopApp/  iosApp/
├── server/             # Server-side service            — not yet initialized
├── docs/
│   ├── adr/            # Architecture decisions
│   ├── prd/            # Product requirements
│   ├── conventions/    # Pointers to cyrillrx/coding-conventions
│   ├── draft-spec.md   # Working ideas, not decisions
│   └── roadmap.md      # V0 / V1 / V2 / V3
├── AGENTS.md           # Contributor guide (human and AI)
└── .github/workflows/  # One workflow per component, filtered by path
```

## Tech stack

| Layer                   | Technology                                                            | Status                                                         |
|-------------------------|-----------------------------------------------------------------------|----------------------------------------------------------------|
| Client                  | KMP / Compose Multiplatform                                           | **Decided** — iOS, Android and Desktop from one codebase       |
| Real-time sync          | Cloud Firestore                                                       | Proposed — [ADR-003](docs/adr/adr-003-persistence-and-sync.md) |
| Local persistence       | Firestore's offline cache, no second store                            | Proposed — [ADR-003](docs/adr/adr-003-persistence-and-sync.md) |
| Auth                    | Firebase Auth, anonymous first                                        | Proposed — [ADR-003](docs/adr/adr-003-persistence-and-sync.md) |
| Server-side service     | `server/` — privileged writes, secrets, notifications, outbound calls | Proposed — [ADR-003](docs/adr/adr-003-persistence-and-sync.md) |
| Notifications           | Telegram Bot                                                          | Candidate                                                      |
| Notification scheduling | Cron (Claude Code Routines, or a self-hosted equivalent)              | Candidate                                                      |
| AI — meal suggestions   | Claude API (Anthropic)                                                | Candidate                                                      |

_Decided_ means an accepted ADR stands behind it. _Proposed_ means the ADR is written but not yet accepted. _Candidate_ is still a working assumption from [`docs/draft-spec.md`](docs/draft-spec.md), to be confirmed or replaced as the project is built.

## Getting started

The client builds with the Gradle wrapper, on Java 21:

```bash
cd cmp-app
./gradlew jvmTest              # Run the tests
./gradlew :desktopApp:run      # Run on Desktop
./gradlew :androidApp:installDebug   # Install on Android
```

For iOS, open `cmp-app/iosApp/iosApp.xcodeproj` in Xcode and run it on a simulator. The full command list is in [`AGENTS.md`](AGENTS.md).

The notification scripts read their configuration from a `.env` file at the repository root:

```bash
cp .env.example .env
# then fill in the Firebase, Telegram and Anthropic values
```

`.env` is git-ignored and must never be committed.

## Documentation

| Document                                   | Content                                                                                     |
|--------------------------------------------|---------------------------------------------------------------------------------------------|
| [`AGENTS.md`](AGENTS.md)                   | Contributor guide — product framing, conventions, repository rules                          |
| [`docs/roadmap.md`](docs/roadmap.md)       | Planned phases (V0 / V1 / V2 / V3)                                                          |
| [`docs/prd/`](docs/prd/)                   | Product requirements — what each feature does, and why                                      |
| [`docs/adr/`](docs/adr/)                   | Architecture decisions and the reasoning behind them                                        |
| [`docs/draft-spec.md`](docs/draft-spec.md) | Draft feature ideas, data model sketch, screens                                             |
| [`docs/conventions/`](docs/conventions/)   | Pointers to [`cyrillrx/coding-conventions`](https://github.com/cyrillrx/coding-conventions) |

## Contributing

Read [`AGENTS.md`](AGENTS.md) first. In short: Conventional Commits in English, atomic commits, one PR per logical change, and no AI attribution in the history.
