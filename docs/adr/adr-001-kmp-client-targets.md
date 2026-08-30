# ADR-001: Kotlin Multiplatform client targeting Android, iOS and Desktop

> **Status**: Accepted | **Date**: 2026-08-29 | **Context**: Client bootstrap, before any feature work.

## Decision

The client is a single Kotlin Multiplatform codebase with a shared Compose Multiplatform UI, living in `cmp-app/` of this monorepo.

Three targets ship: **Android**, **iOS**, **Desktop (JVM)**. There is **no Web target**.

The code is split into two shared modules and three platform wrappers:

```
cmp-app/
├── shared/core/    # domain and data — no Compose dependency
├── shared/ui/      # Compose UI, framework baseName "Shared"
├── androidApp/     # Android application
├── desktopApp/     # JVM application
└── iosApp/         # Xcode project embedding shared/ui
```

Desktop is a **development target, not a product surface**: it is what `jvmTest` and Kover run on, and therefore what feeds coverage to SonarCloud. It is not distributed to users.

## Context

The project needs a client on two phones minimum, kept in sync, built and maintained by one person. Writing and maintaining two native applications was never on the table — the question was only which multiplatform stack, and which targets to enable.

Nothing else in the stack is decided at this point: local persistence, real-time sync, authentication and notifications are all still candidates (see [`../draft-spec.md`](../draft-spec.md)). This ADR covers the client and its targets only.

Two questions had to be answered before generating the project:

1. Which targets does the client ship?
2. Does the shared code live in one module or several?

## Rationale

### Why Kotlin Multiplatform with a shared UI

One codebase for iOS and Android, written in a language already used on the other projects of this account. Compose Multiplatform shares the UI as well, not only the logic, which is what makes a single maintainer realistic for three surfaces.

### Why Desktop is kept even though it is not shipped

Coverage is produced by `jvmTest` and reported through Kover — the same arrangement as `kmp-ttrpg-companion`. Without a JVM target there is no coverage to send to SonarCloud, and the quality gate configured on this project has nothing to read.

Treating Desktop as the target that runs the tests, rather than as a product to maintain, also settles in advance the recurring question of whether it is worth keeping.

### Why no Web target

Adding a target later is cheap. What is not cheap is that **every enabled target restricts the library catalogue**. Persistence, sync and authentication are undecided; enabling `wasmJs` today would rule out any library that does not support it, and the Firebase ecosystem is markedly less mature there than on JVM, Android and iOS. That is a constraint accepted for a surface with no identified use.

Desktop does not cover the same need as Web, despite the shared "large screen, keyboard" shape: the difference is distribution. A desktop application has to be packaged, signed, installed and redistributed on every update. A web application is a URL. If a genuinely used large-screen surface appears — entering recipes with a keyboard, for instance — Web is the answer, not Desktop.

### Why two shared modules instead of one

The wizard generates a single `shared` module holding both logic and Compose UI. Kover excludes composables from coverage, because no Compose UI test feeds it and measuring them would count tests that are never collected. Kover filters by class, and every top-level declaration in a file compiles into a single facade class — so a pure function sharing a file with a composable is excluded along with it, and stops being measured.

Splitting on the module boundary keeps that from happening by construction: `shared/core` has no Compose dependency and is measured in full, `shared/ui` carries the exclusions. Doing it on an empty project costs nothing; doing it later means moving files and rewriting imports.

## Consequences

- **The library catalogue is constrained by three targets, not four.** A library only needs to support Android, iOS and JVM. Adding Web later means re-testing every dependency against `wasmJs` — this is the known cost of the decision, and the reason it is written down rather than left implicit.
- **Testable logic belongs in `shared/core`.** Anything placed next to a composable in `shared/ui` will not be measured, whatever its own test coverage. This is a rule for writing code, not just for organising it.
- **Coverage exclusions are declared twice**, in `kover {}` by class name and in `sonar {}` by file path. An exclusion set on one side only makes the quality gate charge the project for lines it decided not to measure.
- **The iOS framework keeps the baseName `Shared`** even though the module is now `:shared:ui`, so `iosApp/iosApp/ContentView.swift` keeps its `import Shared`. The Xcode build phase invokes `:shared:ui:embedAndSignAppleFrameworkForXcode`.
- **Java 21 across the board** — modules, Android application and CI. Divergence with `kmp-ttrpg-companion`, still on 17, without consequence: the projects share no binary.

## Alternatives considered

**Two native applications (Swift + Kotlin)** — Rejected: doubles the work of every feature, for a single maintainer.

**Kotlin Multiplatform with native UI per platform** — Rejected: shares the logic but not the screens, which is where most of the work is on this product.

**Enabling the Web target now, with Compose/WASM** — Rejected: constrains the library choices before the stack is decided, for a surface with no identified use. Reconsidered when a large-screen need appears.

**Enabling the Web target with Kotlin/JS and React** — Rejected: a second UI to write and maintain. What React would bring — native DOM, SEO, small bundle, accessibility — does not apply to a private application behind authentication.

**Keeping a single `shared` module** — Rejected: makes Kover exclusions unworkable, as described above.
