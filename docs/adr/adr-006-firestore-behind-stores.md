# ADR-006: Firestore is reached through one store interface per aggregate

> **Status**: Accepted | **Date**: 2026-10-17 | **Amended by**: [ADR-008](adr-008-invitation-under-rules.md) | **Context**: [#58](https://github.com/cyrillrx/family-planner/issues/58) is the first repository backed by Firestore, and every repository after it will copy whatever shape it takes.

> [!IMPORTANT]
> [ADR-008](adr-008-invitation-under-rules.md) gave redemption to the client, so the server no longer writes the membership as stated below. It still reads the group's documents to send notifications, which is enough to keep the document types in `core:model`.

## Decision

**A repository never calls the Firebase SDK.** It talks to a **store**: an interface in `data/`, one per aggregate, implemented by a thin adapter that is the only code touching the SDK.

| Layer                         | Lives in                                                    | Holds                                                                          |
|-------------------------------|-------------------------------------------------------------|--------------------------------------------------------------------------------|
| Repository implementation     | `app/shared/domain`, `commonMain`, `domain/`                | Every decision: mapping, defaults, error taxonomy, what a missing field means  |
| Store interface               | `app/shared/domain`, `commonMain`, `data/`                  | The operations the repository needs, named after them, on document types       |
| Store adapter                 | `app/shared/domain`, a source set shared by the SDK targets | Translation to and from the SDK, and nothing else                              |
| Document types                | `core:model`, beside the wire types                         | The shape of a stored document, read by the client and by `server/` alike      |

This is the layout [ADR-004](adr-004-user-identity-and-membership.md) set for an HTTP API — `UserRepositoryImpl` over `UserApi`, wire types in `core:model` — extended to the store. A store is to Firestore what an api is to the service.

**A store is shaped after its aggregate, not after Firestore.** `UserStore.save(user)` and `GroupStore.observe(groupId)`, never `DocumentStore.set(path, map)`. A generic interface would mirror the SDK one call at a time, keep the coupling it claims to remove, and hide the operations each aggregate actually depends on.

**What Firestore knows that the domain needs surfaces as a neutral field**, never as an SDK type. The first one is whether a write is still pending, which PRD-001 requires the screen to show: the adapter reads it from the snapshot metadata and the document type carries it as a boolean.

**The adapter is excluded from coverage, on both sides**, per the coverage policy in `AGENTS.md`. That is only honest while it holds no logic: a branch in an adapter that is not a translation belongs in the repository, where `jvmTest` reaches it.

**The adapter's source set follows the SDK, not the client.** Firebase is declared on Android and JVM only until [#47](https://github.com/cyrillrx/family-planner/issues/47) brings it to iOS, so adapters live in a source set that `androidMain` and `jvmMain` both depend on. When iOS carries the SDK, that source set folds into `commonMain` and no repository moves.

## Context

[#58](https://github.com/cyrillrx/family-planner/issues/58) left the question open in terms: Firestore "has no request and response models and is not transport in that sense. Either it gets a `data/` interface of its own, or the repository implementation uses the SDK directly — ADR-003 only forbids a Firestore type in a *signature*. […] If the answer generalises to every Firestore repository, it is an ADR rather than a line in a PR."

It generalises. The user is the first aggregate; the group, its members, the invitations and every V1 feature follow, each with a repository that will copy the first one.

## Rationale

### The boundary that matters is testability, not portability

Both shapes keep the SDK out of the domain: `UserRepository` is already the interface every use case sees, and [ADR-003](adr-003-persistence-and-sync.md) already forbids a Firestore type in its signature. Portability is a weak argument for a second interface — swapping Firestore for PowerSync, ADR-003's named exit, means rewriting adapters with a store layer and repositories without it, which is a difference of degree.

What decides is where the logic sits. A repository calling the SDK can only be tested against the Firebase emulator, which `jvmTest` does not run and Kover does not see. Its logic would end up untested or excluded — and the logic that grows there is exactly the logic PRD-001 says must be "verifiable in the shared Kotlin tests, without two phones and without a network": what an offline write looks like, what a pending state means, what a missing field becomes. `AGENTS.md` draws the same line from the other side: testable logic belongs in `app/shared/domain`, where `jvmTest` reaches it.

### Document types belong to `core:model`

The server reads and writes the same documents — it writes the membership on redemption, per ADR-003, and will read the group's data to send notifications. A document shape declared on one side only would drift from the other silently, which is the failure [ADR-005](adr-005-kotlin-server.md) moved the wire types into `core:model` to prevent.

## Consequences

- **One interface, one document type and one adapter per aggregate.** The cost is mechanical and grows with the number of aggregates — around four for the MVP.
- **Every repository is testable with a fake store**, the way `UserRepositoryImpl` is tested with a fake `UserApi` today.
- **The adapters are tested by nothing yet.** Thin translations fail loudly on first use rather than subtly, which makes this acceptable for now. Tests against the emulator are the remedy if an adapter ever earns a bug.
- **A store can leak Firestore through its semantics** even without its types — a listener that fires twice, a write that resolves before it reaches the server. The store's contract has to name those behaviours, so the fake reproduces them rather than an idealised store.

## Alternatives considered

**The repository calls the SDK directly** — Rejected: one file fewer per aggregate, but the repository's logic moves out of reach of `jvmTest` and the coverage report, and the whole repository changes source set when iOS gets the SDK instead of a single adapter.

**One generic document store shared by every repository** — Rejected: it reproduces the SDK's surface behind our name, so the repositories stay coupled to Firestore's model, and its fake has to emulate a database rather than a handful of named operations.

**Implementing the existing `UserApi` with Firestore** — Rejected: `UserApi` speaks `ApiResponse`, the envelope of an HTTP call. A store has no envelope and no error payload, and forcing one onto it would invent both.

## Open Questions

- **Do the document types reuse the wire types where they coincide?** `ApiUser` and a stored user may hold the same fields today. Sharing one type couples the service's contract to the store's layout; settled with the first case where they could be shared.
