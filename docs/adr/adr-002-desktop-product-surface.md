# ADR-002: Desktop becomes a secondary product surface

> **Status**: Accepted | **Date**: 2026-09-02 | **Context**: Before the persistence and sync decision, which this one constrains.

## Decision

The Desktop (JVM) application is a **product surface**, distributed to users, ranked second behind iOS and Android.

It keeps its existing role as the target that runs `jvmTest` and produces the coverage SonarCloud reads. That does not change. What changes is that it is no longer *only* that.

This amends [ADR-001](adr-001-kmp-client-targets.md), which stated the opposite: "Desktop is a **development target, not a product surface** […] It is not distributed to users." Every other part of ADR-001 stands — the three targets, the absence of a Web target, the split into `shared/core` and `shared/ui`.

The ranking is part of the decision, not a hedge. Second place means a feature ships on iOS and Android first, and a Desktop-only regression does not block a release. It does not mean the application may be left broken.

## Context

ADR-001 kept Desktop for a purely internal reason: coverage comes from `jvmTest` through Kover, so without a JVM target there is nothing to send to SonarCloud. Treating it as a test harness also settled, at the time, the recurring question of whether it was worth maintaining.

Two things have moved since.

1. The Desktop application exists, builds and runs on every commit. Keeping it undistributed is now a choice being made repeatedly rather than a cost being avoided — the work of having a desktop client has largely been paid, and what remains unpaid is the distribution.
2. The project is a personal one in its early stages, which changes what counts as an acceptable dependency. That matters because the decision below turns the JVM support of every client library into a hard requirement.

ADR-001 answered the large-screen question with "if a genuinely used large-screen surface appears, Web is the answer, not Desktop". This ADR does not claim such a need has been established — no use has been measured, and none is recorded here. It states that when the choice is between an existing target and enabling a new one, the existing target wins on cost.

## Rationale

### Why promote Desktop rather than add Web

Nothing about the Web analysis in ADR-001 has changed: enabling `wasmJs` restricts the library catalogue at the exact moment persistence, sync and authentication are being chosen, and the Firebase ecosystem is markedly less mature there. Desktop, by contrast, is already built, already tested on every commit, and already the target that carries coverage. Promoting it costs a distribution story. Adding Web costs a constraint on decisions not yet made.

### Why second and not equal

The product is used away from a desk — in a shop, in a kitchen, on the way to an activity — and its notification path is Telegram, which involves no desktop at all. Phones are where it earns its keep. Ranking Desktop second states that plainly, so that effort spent on it is a deliberate choice rather than an obligation implied by the phrase "supporting three platforms".

### Why this has to be its own ADR

The promotion turns JVM support into a hard requirement for anything the client depends on. That constraint is an input to the persistence and sync decision, and burying it inside that decision would make it look like a consequence of a database choice rather than the premise it is.

## Consequences

- **Every client dependency must exist on JVM.** This was soft while Desktop was a test harness — a library missing on JVM could have been stubbed out for tests. It is now a filter applied before a library is considered, and it constrains the persistence and sync decision directly.
- **An alpha-quality JVM dependency is acceptable, and is a deliberate risk.** The project is personal and early. A library whose Android and iOS support is stable but whose JVM port is alpha is usable, on the understanding that Desktop is where breakage will surface first. This is exactly the trade the ranking above exists to make bearable.
- **Packaging and distribution become real work, and are not solved by this ADR.** Compose Desktop can produce native distributables (`packageDistributionForCurrentOS`), which covers building them; signing, notarisation on macOS, and how a user receives an update are open. Until they are answered, "distributed" means a locally built artifact and nothing more — an honest limitation rather than an implied store presence.
- **ADR-001's argument against Web loses one of its supports.** That argument contrasted the cost of packaging, signing and redistributing a desktop application with a Web application being merely a URL. Accepting the packaging cost removes the asymmetry. It does not reverse the conclusion, which rests on the library-catalogue constraint, but the Web question is now decided on narrower grounds than before and should be re-read that way.
- **`AGENTS.md` and the `README.md` are corrected**, both of which described Desktop as a non-shipping target.
- **No change to the build, the module layout, ktlint or Kover.** Nothing in the coverage arrangement depends on Desktop's product status.

## Alternatives considered

**Leave Desktop as a test harness and add a Web target for the large-screen need** — Rejected: it constrains the library catalogue exactly when persistence, sync and authentication are being chosen, which is the cost ADR-001 declined to pay and that has not changed.

**Promote Desktop to equal standing with iOS and Android** — Rejected: it would make a Desktop regression a release blocker for a surface the product is not mainly used on, and imply a distribution and update story the project does not have yet.

**Fold this into the persistence ADR as a consequence** — Rejected: the JVM requirement is an input to that decision, not a result of it. Recording it there would invert the reasoning.

**Amend ADR-001 in place** — Rejected: the documentation conventions keep a superseded statement and point at what replaced it, rather than rewriting it. ADR-001 keeps its text and its status line points here.
