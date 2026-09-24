# ADR-005: `server/` is written in Kotlin with Ktor, in one Gradle build

> **Status**: Accepted | **Date**: 2026-09-25 | **Context**: V0 is delivered client-side and has no interlocutor. Five of the seven open issues wait on the service that does not exist yet.

## Decision

**`server/` is a Kotlin application built with Ktor.** This does not reopen [ADR-003](adr-003-persistence-and-sync.md), which rejected "a full custom backend, Ktor **and Postgres**" — what lost there was a backend owning real-time push and offline reconciliation, not Ktor as the shape of the owned service. Its responsibilities are unchanged: invitation redemption, scheduled notification delivery, outbound calls carrying a secret, and custody of every secret.

**The repository builds as one Gradle project, rooted at the repository root.**

```
settings.gradle.kts, gradle/libs.versions.toml, gradlew
core/                                       shared with the server, no client tooling
  model/              :core:model           the entities, and the wire types beside them
app/shared/domain/    :app:shared:domain    repositories, use cases, domain errors
app/shared/ui/        :app:shared:ui
app/androidApp/  desktopApp/  iosApp/
server/               :server               Ktor — api(project(":core:model"))
docs/
```

This **amends two rules in `AGENTS.md`**:

- "Each top-level component owns its own build and its own CI workflow" — components keep their own CI workflow, filtered by path; they no longer keep their own build.
- "Nothing shared between components lives at the root beyond documentation and tooling config" — `core/` and the Gradle build live at the root.

**`core:model` holds what both sides must agree on**, defined by its place in the graph rather than by its contents: it depends on no other module, and both sides depend on it. That is the entities — `User`, `Member`, `Group`, `Invitation` and its three states, the identity value classes, `MIN_CODE_LENGTH` — and the wire types beside them. Not the repositories, not the use cases, not the domain error taxonomies, not Compose. Its only external dependencies are the standard library and `kotlinx-serialization` for the wire types — no coroutines, no Ktor.

The entities and the wire types stay in **one module**: the server needs both, and nothing needs one alone. `core/` around it is a **directory, not a module** — the technical capabilities that will join it, tracking and logging among them, share neither its dependencies, nor its targets, nor its consumers.

It declares **one target per Kotlin platform it serves**: `jvm()`, which serves Desktop, the server and Android alike, plus the iOS targets. No `android {}` block. A Web target joins them if the web client is written in Kotlin; in React it redeclares the contract instead.

The layout follows the Kotlin Multiplatform wizard — `app/`, `core/` and `server/` as siblings — which renames `cmp-app/` to `app/`, and `shared/core` to `shared/domain` now that the entities have left it.

**The error identifiers are fixed here**, because the client invents them today against a service that does not exist:

| Identifier | Meaning |
| --- | --- |
| `invitation_unknown` | No invitation carries this code |
| `invitation_revoked` | Revoked before it was used |
| `invitation_already_redeemed` | Consumed already, by anyone |
| `invitation_expired` | Past its expiry |
| `user_already_in_a_group` | The caller already belongs to a group |

An identifier the client does not recognise maps to `Unknown`, so the service may add one without a client release.

`app/` groups the client surfaces; `core/` and `server/` sit beside it. A web application joins `app/webApp/` whether it is Compose or React — the wizard places it there in both of its templates, as a Gradle module in one and as a Vite project outside Gradle in the other. A component in another technology can still join the root later; nothing here forecloses it.

## Context

[ADR-003](adr-003-persistence-and-sync.md) left this question open in terms: "**What language is `server/` written in?** The draft spec assumes Python. Kotlin with Ktor would let it depend on `shared/core` […] Weighed against Python being the shorter path for scripting and for the `firebase-admin` calls."

It is no longer an academic question. Five of the seven open issues wait on the service:

1. [#13](https://github.com/cyrillrx/family-planner/issues/13) — `RedeemInvitationError.Unknown` conflates three failures, because no `error.id` maps to "this code does not exist". The issue says it plainly: "the three ids are invented ahead of `server/`, so the real contract is settled when the service is written."
2. [#17](https://github.com/cyrillrx/family-planner/issues/17) and [#19](https://github.com/cyrillrx/family-planner/issues/19) — a second invitation can be redeemed, and a joiner can still found a group, in the window before the sync lands. Both conclude the same way: "the reliable barrier is the refusal at the service: it owns membership, so it owns the refusal."
3. [#12](https://github.com/cyrillrx/family-planner/issues/12) — `ApiError.message` has no reader, and will not have one until the real error payload is known.
4. [#16](https://github.com/cyrillrx/family-planner/issues/16) — group creation writes non-atomically, which the Firestore implementation and the service's authority over the member list settle together.

The code says it too. `UserRepositoryImpl` carries, where an error identifier would be read: *The id is deliberately ignored: `server/` names the refusals, and the mapping lands with it.*

## Rationale

### Why Kotlin rather than Python

ADR-003 weighed one advantage against one: a shared model against a shorter path to `firebase-admin`. What has changed since is that the shared model is no longer hypothetical — it exists, it is written, and it is currently duplicated by anticipation.

`InvitationRepositoryImpl` declares `invitation_revoked`, `invitation_already_redeemed` and `invitation_expired` as private constants, invented ahead of the service that will send them. In Python, those three strings would be retyped in a second language and kept in step by proofreading. In Kotlin they are one declaration the compiler checks on both sides, and a renamed identifier breaks the build instead of a user's redemption.

The counterweight has weakened on its own: `firebase-admin` exists for the JVM — ADR-003 established it — so what Python saves is idiom, not capability. One language across the repository is worth more than that, in a project where one person reviews everything.

### Why one Gradle build

`AGENTS.md` requires a build per component. That rule was written when `server/` was assumed to be Python, alongside a KMP client — two toolchains that cannot share a build, so the rule cost nothing and said something useful.

With Kotlin on both sides it only costs. Sharing across two Gradle builds means either a composite build, which duplicates the version catalogue and keeps every coupling anyway, or publishing an artifact, which puts a publication step between two components that move together and destroys the feedback loop where a contract change breaks the other side immediately.

One build is also what the Kotlin Multiplatform wizard produces for exactly this shape — client modules, a shared module, and a Ktor server, all under one `settings.gradle.kts`, the server depending on the shared module by `api(project(...))`. Following it costs nothing and gives anyone who knows the ecosystem a layout they recognise.

### Why `core:model` holds the entities and not only the wire

ADR-003 imagined the server depending on `shared/core` itself. What the server needs from that module is the vocabulary, not the tooling: it validates invitations, so it works with `Invitation` and its states and with `hasExpired`. It has no use for repositories, for use cases, or for `RedeemInvitationError`, which is a taxonomy for whoever calls the client.

Splitting along that line costs little and buys something. `PendingInvitation`, `RevokedInvitation` and `hasExpired` have had no caller in this repository since the invitation simulator was deleted — the service is the caller they were waiting for.

The usual objection to sharing entities is that a shared type can no longer change alone. It applies against a deployed service facing clients that do not update together; it does not apply here. One Gradle build and one repository make a model change a compiler-guided refactor — renaming `MemberId` to `UserId` touched thirteen files and the compiler listed every one.

### Why the wire types survive the shared entities

A domain type and a wire type want opposite things. `RedeemedInvitation` has every field non-null and a `require` in its `init`; `ApiInvitation` has every field nullable, because that is what a wire carries. Merging them forces a choice: either the domain turns permissive, or a malformed answer throws where it now returns `IncompleteResponse(REDEEMED_AT)` naming the missing field.

Serialising a sealed interface of three states would also need configured polymorphism, for a shape that never travels. So both live in `core:model`, and each side translates in its own direction.

### Why `core:model` declares no Android target

An Android consumer accepts a `jvm` variant. `KotlinPlatformType.CompatibilityRule` marks `producer=jvm, consumer=androidJvm` compatible, AGP registers a copy with the same logic, and Gradle's `TargetJvmEnvironmentCompatibilityRules` calls `compatible()` unconditionally. None of them can treat a project dependency differently from a published one: `CompatibilityCheckDetails` exposes the two attribute values and nothing else. `shared/core` already walks that path — its Android classpath resolves `kotlinx-coroutines-core`, which publishes no `androidJvm` variant at all.

The argument holds without the rules. `core:model` has no Android-specific code, and declaring no target makes that structural rather than a matter of discipline. What it gives up — `androidMain`, `withHostTest`, `withDeviceTestBuilder`, Android resources, `android.os.*` — is exactly what a module of entities never uses, and exactly what a `core:logger` will use, in its own module.

Android is the only platform that allows this. A `js`, `wasmJs` or native consumer accepts no `jvm` variant, so those targets have to be declared when their client arrives.

This departs from the wizard, which puts an `android {}` block on `core/` in both templates — the same block on every shared module, whatever it holds. The layout follows the wizard; this one module does not.

### Why the error identifiers belong here rather than in a PRD

They are not product behaviour — what the member sees is "this code no longer works", whatever the reason. They are the vocabulary two pieces of code use to agree, which is what an ADR settles. PRD-001 already routes notification delivery to a future PRD for `server/`; the endpoints and their payloads go there too.

Fixing them now closes #13 and gives #17 and #19 the refusal they ask for, which is the point of writing this ADR before the service rather than after.

## Consequences

- **`:server` needs no Android SDK.** `core:model` applies no AGP, so nothing on the server's path does. Where AGP is applied — `:androidApp` and the two `app/shared` modules — configuring it still costs nothing: only an Android task entering the graph resolves against the SDK, and `:server:build` brings none in. `:desktopApp` already shows it, building with a JDK alone while depending on a module that applies AGP. One caveat: KGP's `jvm*Elements` variants carry no `org.gradle.jvm.version`, so Gradle catches no bytecode-level mismatch — `core:model`'s `jvmTarget` has to stay aligned with the Android app's, or the failure surfaces at dexing rather than at resolution.
- **`kotlinx-serialization` enters the client.** No `Api*` type is `@Serializable` today and the plugin is nowhere in the catalogue. It must exist on the JVM, which [ADR-002](adr-002-desktop-product-surface.md) requires of every client dependency, and it does.
- **`core:model` serves Kotlin consumers only.** A client written in another technology redeclares it or generates it from the running service. That is an argument for keeping it small rather than against sharing it.
- **Three renames follow.** `cmp-app/` becomes `app/`, as the wizard names it. `shared/core` becomes `shared/domain`, which describes what stays in it and frees the word `core` for the root directory. And the entities and the `Api*` types both move into `core:model`, leaving `app/shared/domain` with the repositories, the use cases and the error taxonomies.
- **Three orphan types find a caller.** `PendingInvitation`, `RevokedInvitation` and `hasExpired` have had none since #10 deleted the simulator. Validating a code is the server's job, so they belong to its side of the shared module.
- **Files that describe the old layout have to follow**: `AGENTS.md` (the two amended rules, the structure table, the Commands section, which still says every command runs from `cmp-app/`), `README.md` (the tree, the commands, and "notification scripts" which assumes Python), `.github/workflows/ci-kmp.yml` (paths and `working-directory`), a new `ci-server.yml` on the same model, and the Sonar configuration, which moves with the build to the root.
- **Two leftovers of the Python assumption become wrong.** `.env.example` says its values are "consumed by the scripts in `telegram-bot/`", a directory ADR-003 renamed and that never existed; `.gitignore` carries a `# Python (telegram-bot)` block. `draft-spec.md` keeps a fallback that no longer holds: the same Python scripts run elsewhere by system cron, "no code changes required".
- **The service still fails well.** ADR-003's promise stands: if `server/` is down, synchronization keeps working and the app is unaffected — only notifications and the operations needing an outbound call stop. Nothing here makes the client depend on our uptime, and group creation stays local per [ADR-004](adr-004-user-identity-and-membership.md).

## Alternatives considered

**Python** — Rejected: the draft spec assumed it and ADR-003 credited it a shorter path to `firebase-admin`, but that SDK exists on the JVM too, so what it saves is idiom. Against it: a second language to maintain, and a contract that cannot be shared — the error identifiers would be retyped and kept in step by proofreading.

**Two Gradle builds, joined by a composite build** — Rejected: it satisfies a rule written under an assumption that no longer holds, duplicates the version catalogue, and keeps every coupling it claims to break.

**Two Gradle builds, sharing through a published artifact** — Rejected: it puts a publication step between two components that move together, and loses the property that matters most — a contract change breaking the other side at once.

**Sharing nothing, each side declaring its own wire types** — Rejected: it removes the one advantage ADR-003 credited to Kotlin, and an identifier renamed on one side breaks the other silently.

**Sharing the wire types only, leaving the entities to each side** — Rejected: it leaves two definitions of what an invitation is, and leaves `PendingInvitation`, `RevokedInvitation` and `hasExpired` without a caller on either side of a line drawn through the middle of one concept.

**Depending on `shared/domain` directly, as ADR-003 imagined** — Rejected: that module holds the repositories, the use cases and the client's error taxonomies. The server would inherit tooling it has no use for, and the client's domain would become answerable to it.

**Flattening the client surfaces to the repository root** — Rejected: `app/` groups what ships to a user, which is what the wizard does and what keeps `core/` and `server/` legible beside it.

**Splitting the entities from the wire types, one module each** — Rejected: the server depends on both, nothing depends on one alone, and a second module is indexing overhead for no boundary anyone enforces. The split that does hold, under `core/`, is between the contract and the technical capabilities beside it.

**One `core` module rather than a directory** — Rejected: tracking and logging will live there too, with dependencies and targets of their own. Neither belongs in the module the server consumes.

**Giving `core:model` an Android target, as the wizard does** — Rejected: nothing requires it, it binds the module to the AGP version through `AgpVersionAttr`, and it opens an `androidMain` to platform code that has no place among entities.

## Open Questions

- **Where does `server/` run?** Choosing Ktor assumes a long-running service rather than a scheduled script, which narrows the field without settling it. The decision and what it costs to operate — availability for redemption, a schedule for notifications, how secrets reach the host — belong in their own ADR.
- **What does the service expose, and how does it authenticate?** Endpoints, payloads and the verification of the caller's token belong to the PRD that PRD-001 already promises `server/`. [ADR-004](adr-004-user-identity-and-membership.md) fixes one constraint in advance: every call is signed by whoever makes it, so the service derives the identity from the token rather than from a forgeable body.
