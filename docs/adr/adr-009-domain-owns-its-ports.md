# ADR-009: The domain owns the interfaces it depends on, and a store reports its failure as a value

> **Status**: Accepted | **Date**: 2026-11-16 | **Amends**: [ADR-004](adr-004-user-identity-and-membership.md), [ADR-006](adr-006-firestore-behind-stores.md) | **Context**: [#94](https://github.com/cyrillrx/family-planner/issues/94) gave the stores typed results, which made the domain name a store failure, and placed the store interfaces where ADR-006 said they live: in `data/`, below the domain that defines their contract.

## Decision

**Dependencies point toward the domain: UI → domain ← data.** The domain knows neither the UI nor the data layer. The data layer knows the domain, and implements what the domain asks of it.

| Layer     | Holds                                                                                                  | Knows        |
|-----------|--------------------------------------------------------------------------------------------------------|--------------|
| `domain/` | Entities, repositories, use cases, and the interfaces a repository depends on: its stores and its apis | nothing else |
| `data/`   | The adapters implementing those interfaces, and the transport types they translate                     | `domain`     |

**A store reports its failure as a value, never as an exception.** Each store operation answers a `Result` whose failure is `StoreUnavailable`: the store was out of reach, refused the call, or could only answer from a cache that cannot confirm an absence. Translating an SDK exception into that value is translation, so it is the adapter's work. Any other exception is a defect and is not caught there.

**The repository keeps the taxonomy.** It decides what an absence means, and it reports a defect of the client as `Unexpected`, apart from a store that gave no answer. Both are a `NoAnswer`: the call tells nothing about what the store holds.

**The api interfaces follow when they are next touched.** `UserApi` and `InvitationApi` still sit in `data/`, as ADR-004 placed them; moving them into the domain is tracked as a follow-up, and nothing new goes into `data/` as an interface.

## Context

[ADR-004](adr-004-user-identity-and-membership.md) drew the layers as UI → domain → data, with `data/` holding the api interfaces and knowing nothing of the domain. [ADR-006](adr-006-firestore-behind-stores.md) extended that layout to the store, and kept the error taxonomy in the repository with a store that has "no envelope and no error payload".

Both held while a store failure was an exception. Once a store answers a typed result, its failure is a domain value, and an interface in `data/` either names a domain type, which ADR-004 forbids, or invents a failure type of its own that the repository translates a second time.

## Rationale

**The contract belongs to whoever needs it.** A store exists because a repository needs a handful of named operations ([ADR-006](adr-006-firestore-behind-stores.md)); its shape is dictated by the domain, not by Firestore. Declaring it in the domain says so, and makes an adapter the only code that has to change when the SDK does.

**One failure value, read once.** `StoreUnavailable` is what every adapter agrees to report, and the repository maps it into the taxonomy. A store that threw left each repository to know which exceptions mean "no answer", which is SDK knowledge in the domain.

**What ADR-004 protected still holds.** Its concern was a wire type carrying a domain type. The transport types stay transport-shaped; only the interface moves, and the adapter translates between the two.

## Consequences

- **An adapter imports the domain**: the store interface, its document types and `StoreUnavailable`. A domain file never imports `data/`.
- **Every store operation answers a `Result`**, and its fake answers the same failure, so the repository's handling of an unreachable store is tested in `jvmTest`.
- **The api interfaces disagree with this ADR until they move.** The follow-up closes the gap; until then, a new api is declared in the domain.

## Alternatives considered

**Keep the interfaces in `data/` with a failure type of their own.** Rejected: the repository would translate a data failure into a domain one that says the same thing, and the data layer would still define a contract only the domain uses.

**Let a store throw, and catch in the repository.** Rejected: the repository would have to know the SDK's exceptions to tell an unreachable store from a defect, which is the coupling ADR-006 removed.
