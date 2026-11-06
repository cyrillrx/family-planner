# ADR-008: Invitations are redeemed under Firestore rules, not by the server

> **Status**: Accepted | **Date**: 2026-10-25 | **Amends**: [ADR-003](adr-003-persistence-and-sync.md), [ADR-004](adr-004-user-identity-and-membership.md), [ADR-005](adr-005-kotlin-server.md), [ADR-006](adr-006-firestore-behind-stores.md) | **Context**: The group, its memberships and its invitations are still in memory ([#77](https://github.com/cyrillrx/family-planner/issues/77)), and ADR-003 routes redemption through a `server/` that nothing hosts yet.

## Decision

**A client redeems an invitation itself, and the security rules decide whether it may.** `server/` no longer takes part in joining a group. It keeps the notifications, the outbound calls that carry a secret, and the custody of those secrets.

**Redemption is one transaction.** It writes the redeemer's membership and marks the invitation redeemed, and the rules accept each write only alongside the other: the membership against an invitation of the same group that was pending and unexpired before the write, the invitation only once that membership exists. Single use rests on that check of the stored state, which Firestore commits atomically: whoever writes second, transaction or not, finds the invitation redeemed. The transaction only makes the client fail fast. Beyond being redeemed or revoked, an invitation never changes, so its single use and its expiry cannot be rewritten.

**Founding a group has the same shape.** The group and its founder's membership are one batch, and the rules accept a membership without an invitation only alongside the creation of a group that did not exist. This settles [ADR-004](adr-004-user-identity-and-membership.md)'s open question: the founder writes their own membership, and the anchor the client cannot forge is that the group did not exist.

**Memberships are addressed by credential, and authorization reads the credential, never the `UserId`.** Every membership is written by its own credential, so nobody enrols someone else, and every rule asks whether `groups/{groupId}/members/{request.auth.uid}` exists. The membership also carries its credential as a field, so a device finds its groups by a collection-group query on that field, which a rule can check against `request.auth.uid`. The `UserId` on the membership is attribution. [#68](https://github.com/cyrillrx/family-planner/issues/68), a device claiming another member's `UserId`, stays open, but as a problem of attribution rather than of access.

| Path                                    | Holds                                                                                   | Addressed by                 |
|-----------------------------------------|-----------------------------------------------------------------------------------------|------------------------------|
| `groups/{groupId}`                      | The group                                                                               | Our identifier               |
| `groups/{groupId}/members/{credential}` | A membership: its credential, our `UserId`, its group, the join time, the code redeemed | The credential of the member |
| `invitations/{code}`                    | Its group, creation time, redemption and revocation                                     | The code: 128 random bits    |

**The rules compare server time only.** Creation, join, redemption and revocation are written as server timestamps, and an invitation expires seven days after its creation, so no client clock enters the decision.

**Knowing the code is the authorization to read its invitation.** Any signed-in caller may read one by its code; nobody may list them.

## Context

[ADR-003](adr-003-persistence-and-sync.md) made redemption the one privileged operation and gave it to an owned service, because "security rules deny every client write to the group's member list. Without that denial the service is a formality." Where that service runs was left open, and [ADR-005](adr-005-kotlin-server.md) narrowed it to a long-running host without choosing one.

The MVP needs two people in one group before it needs anything else. On the path ADR-003 drew, that waits for a host, a deployment, Firebase Admin credentials in it, and token verification on every call — operations work that is not the product, for a single endpoint. Meanwhile [#16](https://github.com/cyrillrx/family-planner/issues/16) is blocked on the founder's membership, which ADR-004 could not place either: the rules denying the member list deny the founder too.

## Rationale

### Rules can hold the guarantees ADR-003 asked of the service

ADR-003 lists what the invitation must be: unguessable, single-use, expiring, revocable, validated together with the membership it grants, and enforced by something other than the client. Each holds under rules:

| Guarantee             | Held by                                                                                    |
|-----------------------|--------------------------------------------------------------------------------------------|
| Unguessable           | 128 random bits drawn by a member's client, which PRD-001 trusts; listing is denied        |
| Single-use            | The rule requires an invitation still pending before the write                             |
| Expiring              | Computed in the rule from the stored server timestamp                                      |
| Revocable             | A revoked invitation fails the same check as a redeemed one                                |
| Atomic with the grant | The rules tie the membership and the invitation within one write, in both directions       |
| Not up to the client  | Rules run on Firestore's side and bypass nothing                                           |

The denial ADR-003 insisted on is kept, only narrowed: a client still cannot write a membership on its own, only as half of a write whose other half is checked. Leaving a group and deleting one stay denied until the features that bring them.

### It fails better than the service would

ADR-003 promised that "if the owned service is down, synchronization keeps working and the app is unaffected" — and redemption was the exception, since joining needed our service. Under rules, joining needs Firestore and nothing else, so the product's core path depends on no availability of ours at all.

It also shrinks the one unguarded place ADR-003 warned about: a service holding administrative credentials bypasses every rule, and a bug in it is unguarded. Redemption was the reason that service needed write access to the member list. Sending notifications needs only to read.

### Addressing memberships by credential

[ADR-003](adr-003-persistence-and-sync.md) keeps our `UserId` apart from the Firebase UID so that identity survives leaving Firebase, and that is unchanged — the `UserId` is still on the membership. The address is a different matter: ADR-003 says "Firestore document paths are addressing, not identity", and the users collection is already addressed by credential. Addressing memberships the same way is what lets a rule check membership with one `exists()`, without trusting a `UserId` the user document lets anyone claim.

[ADR-007](adr-007-account-linking.md)'s linking keeps the UID, so a member who signs in keeps their membership's address. One who signs in to an existing account takes that account's UID, and finds that account's memberships.

## Consequences

- **`server/` has no endpoint on the MVP's path.** Its hosting stops blocking the group, and becomes a question for the notifications.
- **The rules carry logic, against ADR-003's "authorization expressed as rules, not as logic".** Redemption and founding, the two ways into a group, now live in a language `jvmTest` does not reach. They are the one exception, small and named here.
- **The rules need tests of their own.** The Firestore emulator runs them; nothing in CI does yet. Until it does, a regression there opens the group to whoever holds an old code.
- **The client tells the member why; the rules decide whether.** A refusal reaches the client as a bare `PERMISSION_DENIED`, so the reason shown comes from the invitation the transaction read.
- **Joining needs the network**, as it did under ADR-003. A refused write queued offline would fail later with nobody watching, so the writes others can contest — redeeming, creating and revoking an invitation — are transactions, which fail at once offline. Founding a group contests nothing and works offline.
- **The code is a document address.** It appears in the Firebase console and in Firestore's own request logs, which ADR-003's "never written to any log" did not contemplate. Those are ours to read; it still never reaches an application log, an analytics event or a crash report.
- **The rules do not hold a credential to one group.** Belonging to several groups is where the product is going, and each membership is still earned by its own invitation or founding. One group per member is a V1 limit the client keeps; [#17](https://github.com/cyrillrx/family-planner/issues/17) and [#19](https://github.com/cyrillrx/family-planner/issues/19) are the windows where it does not yet.
- **The decision is reversible.** Moving redemption back to a service means denying the client the membership writes the rules now accept. The layout, addressed by credential, serves either way.

## Alternatives considered

**Hosting `server/` now, as ADR-003 planned** — Rejected for the MVP: a host, a deployment and administrative credentials for one endpoint the rules can replace, and the core path tied to our availability. It stays the route if redemption ever needs something rules cannot express — rate limiting, or a side effect outside Firestore.

**A Cloud Function triggered on redemption** — Rejected: it needs the Blaze plan, and it reintroduces a privileged writer that bypasses the rules, which is the surface this decision removes.

**Memberships addressed by `UserId`** — Rejected: every rule would have to read the user document to map the caller's UID to a `UserId`, and would then trust a value [#68](https://github.com/cyrillrx/family-planner/issues/68) shows anyone can claim.

**The code's hash as the document address** — Rejected for now: it keeps the code out of the console, at the cost of hashing on the client and in the rules. Worth revisiting if the console ever becomes shared.

## Open Questions

- **How does a member read another member's name?** [ADR-004](adr-004-user-identity-and-membership.md) scopes `/users` reads by shared membership, but `users/{credential}` names no group, so a rule cannot test it. Decided with [#77](https://github.com/cyrillrx/family-planner/issues/77).
- **Does the Desktop SDK carry this?** Transactions, server timestamps and collection-group queries have to work through `firebase-java-sdk`, which [ADR-007](adr-007-account-linking.md) found missing methods in. Batched writes, server timestamps and collection-group queries were checked against the live project with [#80](https://github.com/cyrillrx/family-planner/pull/80): a group founded on Desktop is found again on the next launch. Transactions remain to check with the redemption, in [#77](https://github.com/cyrillrx/family-planner/issues/77).
