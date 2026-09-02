# family-planner

KMP/CMP mobile app (iOS + Android) for managing daily family life. Reduces mental load by centralizing meal planning, shared events and shared tasks.

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
├── telegram-bot/       # Notification scripts            — not yet initialized
├── docs/
│   ├── adr/            # Architecture decisions
│   ├── prd/            # Product requirements
│   ├── conventions/    # Pointers to cyrillrx/coding-conventions
│   ├── draft-spec.md   # Working ideas, not decisions
│   └── roadmap.md      # V1 / V2 / V3
├── AGENTS.md           # Contributor guide (human and AI)
└── .github/workflows/  # One workflow per component, filtered by path
```

## Tech stack

| Layer                   | Technology                                               | Status                                        |
|-------------------------|----------------------------------------------------------|-----------------------------------------------|
| Mobile client           | KMP / Compose Multiplatform                              | **Decided** — iOS + Android from one codebase |
| Local database          | SQLDelight                                               | Candidate                                     |
| Real-time sync          | Firebase Firestore                                       | Candidate                                     |
| Auth                    | Firebase Auth                                            | Candidate                                     |
| Notifications           | Telegram Bot                                             | Candidate                                     |
| Notification scheduling | Cron (Claude Code Routines, or a self-hosted equivalent) | Candidate                                     |
| AI — meal suggestions   | Claude API (Anthropic)                                   | Candidate                                     |

Only the client stack is settled. Everything marked _Candidate_ is a working assumption from [`docs/draft-spec.md`](docs/draft-spec.md) and will be confirmed — or replaced — as the project is built, with an ADR for each decision that warrants one.

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
| [`docs/roadmap.md`](docs/roadmap.md)       | Planned phases (V1 / V2 / V3)                                                               |
| [`docs/prd/`](docs/prd/)                   | Product requirements — what each feature does, and why                                      |
| [`docs/adr/`](docs/adr/)                   | Architecture decisions and the reasoning behind them                                        |
| [`docs/draft-spec.md`](docs/draft-spec.md) | Draft feature ideas, data model sketch, screens                                             |
| [`docs/conventions/`](docs/conventions/)   | Pointers to [`cyrillrx/coding-conventions`](https://github.com/cyrillrx/coding-conventions) |

## Contributing

Read [`AGENTS.md`](AGENTS.md) first. In short: Conventional Commits in English, atomic commits, one PR per logical change, and no AI attribution in the history.
