# ADR-004: User identity is separated from group membership

> **Status**: Proposed | **Date**: 2026-09-09 | **Context**: V0 onboarding, before implementation — writing the first caller of the group domain exposed a hole in it.

## Decision

**A person and their place in a group are two types.** `User` is the identity; `Member` is the membership, and carries nothing else.

```kotlin
// group/domain/
data class User(val id: UserId, val displayName: String, val authenticatedId: String? = null)
data class Member(val userId: UserId, val groupId: GroupId, val joinedAt: Instant)
data class Group(val id: GroupId, val name: String, val createdAt: Instant)
```

`MemberId` is removed. A membership is identified by the pair (groupId, userId): two memberships of the same user in the same group are meaningless, so a separate identifier would have nothing to distinguish. `RedeemedInvitation.redeemedBy` is a `UserId`, and `InvitationApi.redeem` takes a `UserId`.

**The display name is not duplicated.** It lives on `User`, in one copy. Listing a group's members is a join, not a read of one collection.

**The group carries no creator.** There is no `createdBy` field. Creation order is not a product concept in V1.

**API interfaces belong to the data layer, repository interfaces to the domain.**

| Layer | Holds |
| --- | --- |
| `domain/` | Entities, repository interfaces, use cases, `CurrentUserStore` |
| `data/` | API interfaces, request and response models, repository implementations, mappers |

An API interface speaks in `Api*` request and response types, and those live in `data`. Declaring such an interface in `domain` would pull the data layer into it. Repository interfaces stay in `domain` because they speak the domain. `InvitationApi` therefore moves from `group/domain/` to `group/data/`, with an `InvitationRepository` in the domain in front of it, and use cases depend on `domain` alone.

**Registration precedes redemption.** Joining a group runs in this order, and the domain enforces it:

1. `register(displayName)` — establishes the user and remembers it on this device
2. `joinGroup(code)` — refuses with `NotRegistered` if no user is registered, then redeems

The invitation is single-use and unrecoverable, so it is consumed last. `joinGroup` never writes a membership itself; the owned service does, per [ADR-003](adr-003-persistence-and-sync.md).

**Onboarding remains an explicit choice.** No group is created at launch to be reconciled later.

## Context

[PRD-001](../prd/prd-001-group-and-synchronization.md) requires an invited person to land in the shared data rather than in a group of their own, and makes the display name "the only thing about them the other members see". [ADR-003](adr-003-persistence-and-sync.md) makes invitation redemption the one privileged operation: the owned service "validates and writes the membership in one transaction", and "security rules deny every client write to the group's member list. Without that denial the service is a formality."

Writing the first caller of the group domain — the onboarding use case — put those two requirements against each other. `redeem(code, member: MemberId)` carries an identifier and nothing more, so the service has no name to write. The client cannot supply one by writing the membership itself, because that write is exactly what the rules deny. Widening the call was the obvious repair, and it is the wrong one: it treats the symptom.

The cause is that `Member` conflates **who a person is** — their name, and the credential that will authenticate them in Phase 2 — with **their place in a group**, the moment they joined. While those are one type, every call that creates a membership must also carry an identity, and every store of memberships is also a store of people.

Two questions follow, and each gets a rationale section: where the name lives once it is not on the membership, and where the boundary between the domain and its transports falls.

## Rationale

### Why membership carries no name

Once the display name is on `User`, the membership has nothing left to name. The service writes `Member(userId, groupId, joinedAt)` from three values it already holds: the `groupId` on the redeemed invitation, the `userId` in the call, and its own clock. The name was never missing from the call — it was in the wrong place, and the client writes it to its own user document, which the member-list rule does not cover.

Keeping a copy of the name on the membership would restore the single read that a document store rewards, and is refused: two copies of a name mean a rename has to sweep every membership of that user, and a missed sweep shows other members a stale name with nothing to say it is stale.

### Why `MemberId` disappears

`redeem(code, member: MemberId)` asked the caller for the identifier of a membership that does not exist yet — the person redeeming an invitation is, by definition, not a member. With the pair (groupId, userId) as the key, the call takes the identifier of something that does exist, and the type system stops expressing a state the product cannot be in.

### Why the order is register then redeem

The two operations fail differently. A failed registration costs a user record that no group points to — harmless, and reused on the next attempt, since registration is idempotent. A failed write after a successful redemption costs the invitation itself, which is single-use and cannot be reissued to the same person. Consuming the irrecoverable resource last is what makes a retry safe.

This is a guarantee of sequence, not of type, and it is enforced by `NotRegistered`: `joinGroup(code)` will not run without a registered user. Nothing in a signature could express it.

### Why API interfaces live in the data layer

An API interface is defined by its wire types. `UserApi.register(request: ApiRegisterUserRequest): ApiUser` names two types whose fields are dictated by the transport, not by the product — a request omits what the server derives, a response carries what the server decides. Those types belong to `data`.

Placing the interface in `domain` would put those types in `domain` too, or force the interface to speak domain types and mediate the mapping somewhere unnamed. Repository interfaces do not have the problem: they speak the domain by construction, which is why they stay where they are and why the use cases only ever see them.

### Why the group carries no creator

V1 has no roles: PRD-001 states that "all members hold the same rights over shared data. There are no roles and no owner privileges in V1", and that "the group survives the departure of the person who created it. There is no owner privilege to inherit." No behaviour reads the creator, so the field would be inert — and worse than inert, because a `createdBy` on the aggregate invites the next reader to treat the creator as an owner, which is the notion the product refuses.

PRD-001's attribution requirement — "every shared record carries its group, the moment it was last changed, and who changed it" — is in the *Synchronization* section and applies to shared records; `Group`, `Member` and `Invitation` are not shared records in that sense, which is why the sync envelope was removed from the domain before it shipped. Showing an author in the UI is a Phase 2 requirement.

Audit trails — `createdAt`, `updatedAt`, the author of a write — remain a persistence concern. The author of a write does not need the domain to be known: every call to the owned service is signed by whoever makes it, so the service derives the identity from the token rather than reading it out of a forgeable body.

## Consequences

- **Listing members is a join.** `observeMembers()` yields memberships; the names come from `UserRepository.observeUsers(ids)`. On Firestore that is a second query with an index, against ADR-003's expectation that "anything needing joins across the group's data will be denormalised". This ADR pays that cost deliberately, and only here: one name per person is worth one query.
- **The join can be incomplete, and that is representable.** A membership may be visible before its user document has synced, so the read model pairs a `Member` with a nullable `User`. PRD-001 already requires the app to say when what is on screen may be stale.
- **`/users` needs read rules of its own**, scoped by shared membership rather than open — a cost in rule `get()` calls that the denormalised shape would not have had.
- **`InvitationApi` moves**, and every future API follows the same rule. The convention is now written, so the next transport does not reopen the question.
- **The guard on group creation must not block cold.** `createGroup()` refuses when a group already exists, which means reading the current value. A production repository has to emit from cache — `null` included — without waiting for the server, or the first launch offline hangs.
- **PRD-001 is revised to v0.4** in the same change: three requirements phrase identity as a property of the membership, which is what produced this hole, and a vocabulary entry is added under *Membership*.
- **Phase 2 gains a clearer target.** ADR-003 keeps our identifier and the Firebase UID as two values so that leaving Firebase stays possible; both now sit on `User`, next to each other, instead of on a record that also encodes a group. Linking an anonymous identity to an account touches one type.

## Alternatives considered

**Denormalising the display name onto `Member`** — Rejected: the single read it buys costs two copies of every name, and a rename that must sweep each membership. A stale name is invisible to the member reading it.

**Widening `redeem` to carry the name** — Rejected: it repairs the call and leaves the cause, a type that means two things. Every later operation creating a membership would carry the same passenger.

**Routing group creation through `server/`** — Rejected: it makes the very first launch depend on our availability and impossible offline, against PRD-001's offline-first requirement and against ADR-003's own promise that "if the owned service is down, synchronization keeps working and the app is unaffected". Creating a group is local; joining one needs the network because its validation is privileged by construction.

**Creating a default group at launch and reconciling later** — Rejected: PRD-001 forbids it in terms — "the app never creates a group silently" — because an invited person who already holds a group of their own cannot be extracted from it. The problem it would solve does not exist: `createGroup()` writes to the local cache and the UI confirms at once.

**Keeping `createdBy` on `Group`** — Rejected: no V1 behaviour reads it, and it suggests an owner privilege that PRD-001 denies twice. Audit belongs to persistence.

**A `MemberId` alongside `UserId`** — Rejected: with (groupId, userId) as the key, it would identify nothing that the pair does not.

## Open Questions

- **What authorizes a founder to write their own membership?** ADR-003 denies every client write to the member list, which the founder's own first membership contradicts. The answer is persistence, not domain: a rule anchored on a value the client cannot forge, or a signed write through `server/`. It is decided with the real rules in hand; nothing is deployed, so deferring it costs no migration.
- **Does a user document exist before any group?** Registration creates one, and a person who registers then abandons onboarding leaves it behind. Harmless while it is one document per device, and worth revisiting if it ever needs cleaning up.
- **Where does the display name get edited?** `UserRepository.register` is idempotent and updates the name, which makes it the accidental answer. A deliberate one belongs to a settings PRD.
