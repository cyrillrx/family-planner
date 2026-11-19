# ADR-009: The domain owns the interfaces it depends on, and a store reports its failure as a value

> **Status**: Accepted | **Date**: 2026-11-16 | **Amends**: [ADR-004](adr-004-user-identity-and-membership.md), [ADR-006](adr-006-firestore-behind-stores.md) | **Context**: [#94](https://github.com/cyrillrx/family-planner/issues/94) gave the stores typed results, which made the domain name a store failure, and placed the store interfaces where ADR-006 said they live: in `data/`, below the domain that defines their contract.

## Decision

**Dependencies point toward the domain: UI → domain ← data.** The domain knows neither the UI nor the data layer. The data layer knows the domain, and implements what the domain asks of it. The composition root (`AppGraph`) is the one exception: it knows the adapters, to hand them to the repositories.

| Layer     | Holds                                                                                                  | Knows        |
|-----------|--------------------------------------------------------------------------------------------------------|--------------|
| `domain/` | Entities, repositories, use cases, and the interfaces a repository depends on: its stores and its apis | nothing else |
| `data/`   | The adapters implementing those interfaces                                                             | `domain`     |

The document types and the wire types stay in `core:model`, where [ADR-005](adr-005-kotlin-server.md) and [ADR-006](adr-006-firestore-behind-stores.md) put them, since the server reads them too.

**A store reports its failure as a value, never as an exception.** Each store operation answers a `Result` whose failure is `StoreUnavailable`: the store gave no answer the domain can use. Translating an SDK exception into that value is translation, so it is the adapter's work. Any other exception is a defect and is not caught there.

**The repository keeps the taxonomy.** It decides what an absence means, and tells a defect of the client apart from a store that gave no answer.

**The api interfaces follow when they are next touched.** `UserApi` and `InvitationApi` still sit in `data/`, as ADR-004 placed them; moving them into the domain is tracked as a follow-up, and nothing new goes into `data/` as an interface.

## Context

[ADR-004](adr-004-user-identity-and-membership.md) drew the layers as UI → domain → data, with `data/` holding the api interfaces and knowing nothing of the domain. [ADR-006](adr-006-firestore-behind-stores.md) extended that layout to the store, and kept the error taxonomy in the repository with a store that has "no envelope and no error payload".

Both held while a store failure was an exception. Once a store answers a typed result, its failure is a domain value, and an interface in `data/` either names a domain type, which ADR-004 forbids, or invents a failure type of its own that the repository translates a second time.

## Rationale

**The contract belongs to whoever needs it.** A store exists because a repository needs a handful of named operations ([ADR-006](adr-006-firestore-behind-stores.md)); its shape is dictated by the domain, not by Firestore. Declaring it in the domain says so, and makes an adapter the only code that has to change when the SDK does.

**One failure value, read once.** `StoreUnavailable` is what every adapter agrees to report, and the repository maps it into the taxonomy. A store that threw left each repository to know which exceptions mean "no answer", which is SDK knowledge in the domain.

**What ADR-004 protected still holds.** Its concern was a wire type carrying a domain type. The wire types stay transport-shaped; only the interface moves, and the adapter translates between the two.

## Consequences

- **An adapter imports the domain**: the store interface and `StoreUnavailable`, beside the document types of `core:model`. A domain file never imports `data/`.
- **Every store operation answers a `Result`**, and its fake answers the same failure, so the repository's handling of an unreachable store is tested in `jvmTest`.
- **What counts as an outage rests on the SDK's exception types.** The Firestore adapter reports a `FirebaseException` as `StoreUnavailable`; an outage the SDK raises as another exception reads as a defect. That translation sits in the adapter, which no test reaches.
- **A defect gets the treatment of an outage.** Both leave the domain without an answer, so the screen offers the same retry for both, and a retry after a defect fails again. Telling them apart on screen, or reporting the defect, is the remedy if it ever matters.
- **The api interfaces disagree with this ADR until they move.** The follow-up closes the gap; until then, a new api is declared in the domain.
- **Reverting is mechanical but wide**: every store, adapter and fake changes its signatures, and the interfaces move back.

## Alternatives considered

**Keep the interfaces in `data/` with a failure type of their own.** Rejected: the repository would translate a data failure into a domain one that says the same thing, and the data layer would still define a contract only the domain uses.

**Let a store throw the SDK's exceptions, and catch in the repository.** Rejected: the repository would have to know the SDK's exceptions to tell an unreachable store from a defect, which is the coupling ADR-006 removed.

**Let the adapter throw an exception the domain declares.** The translation stays in the adapter, and the repository catches one type. Rejected: an exception is not in the signature, so the compiler never makes the repository handle an unreachable store, and a fake reproduces one only when its author thinks of it.
