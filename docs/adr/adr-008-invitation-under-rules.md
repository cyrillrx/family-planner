# ADR-008: Invitations are redeemed under Firestore rules, not by the server

> **Status**: Accepted | **Date**: 2026-10-25 | **Amends**: [ADR-003](adr-003-persistence-and-sync.md), [ADR-004](adr-004-user-identity-and-membership.md), [ADR-005](adr-005-kotlin-server.md), [ADR-006](adr-006-firestore-behind-stores.md) | **Context**: The group, its memberships and its invitations are still in memory ([#77](https://github.com/cyrillrx/family-planner/issues/77)), and ADR-003 routes redemption through a `server/` that nothing hosts yet.

## Decision

**A client redeems an invitation itself, and the security rules decide whether it may.** `server/` no longer takes part in joining a group. It keeps the notifications, the outbound calls that carry a secret, and the custody of those secrets.

Redemption is one transaction: it reads the invitation, which names the group, then makes two writes that the rules accept only together:

1. The redeemer's membership, carrying the code it was redeemed with.
2. The invitation, marked redeemed by the redeemer's credential, at the server's time.

The rules accept the membership only if the invitation it names belongs to the same group, was neither redeemed nor revoked before the transaction, has not expired, and is redeemed by the same credential once the transaction applies. They accept the invitation's update only if it was pending before the transaction, changes nothing but its redemption, and the redeemer's membership exists once the transaction applies and names that code. Two people cannot consume the same code: the second transaction reads an invitation already redeemed.

An invitation changes in no other way than being redeemed or revoked. A revocation is accepted from a member of its group, on a pending invitation, and changes nothing but its revocation. Without those limits, single use and expiry would hold only until someone rewrote the invitation.

**Every membership is created by its own credential.** Its document id and its `credential` field both equal `request.auth.uid`, so nobody enrols someone else, and nobody points another device's collection-group query at their group.

**The client tells the member why, the rules decide whether.** A refusal reaches the client as a bare `PERMISSION_DENIED`, so the reason comes from the invitation the transaction read: missing, revoked or already redeemed. A refusal of an invitation still pending reads as expired when it was created more than seven days ago, and as a plain refusal otherwise. That reading is a display, like the expiry date the client shows; it never lets a redemption through.

**Creating a group is the same shape.** The group and its founder's membership are one batch. The rules accept a membership without an invitation only alongside the creation of its group, which must not exist before the batch, and accept a group only alongside its founder's membership, so no group is ever left without a member. This settles [ADR-004](adr-004-user-identity-and-membership.md)'s open question: the founder writes their own membership, and the anchor the client cannot forge is that the group did not exist.

**The layout**, every path but the invitation's scoped by group so a second group changes nothing in it; an invitation names its group in its fields:

| Path                                    | Holds                                                                                                        | Addressed by                                         |
|-----------------------------------------|--------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| `groups/{groupId}`                      | The group: its name and creation time                                                                        | Our identifier                                       |
| `groups/{groupId}/members/{credential}` | A membership: our `UserId`, the credential, its group, the join time, the code redeemed (none for a founder) | The credential of the member                         |
| `invitations/{code}`                    | Its identifier, its group, creation time, redemption and revocation                                          | The code: 128 random bits, base64url without padding |

**Authorization reads the credential, never the `UserId`.** A membership is addressed by the Firebase UID that wrote it, and every rule asks whether `groups/{groupId}/members/{request.auth.uid}` exists. The `UserId` on the membership is attribution, checked against the member's own user document at write time, so a redemption waits until the server has acknowledged that document, as a device registered offline may not have sent it yet. [#68](https://github.com/cyrillrx/family-planner/issues/68) — a device claiming another member's `UserId` — stays open, but as a problem of attribution rather than of access.

**A device finds its group by a collection-group query** on the memberships carrying its credential. The rule reading it is scoped to the caller's own memberships by testing the `credential` field, since a query filter cannot prove a document id, and the query needs a collection-group index on that field, versioned beside the rules.

**Every time the rules compare is the server's.** Creation, join, redemption and revocation are written as server timestamps, and the rules require them to equal the request time. An invitation expires **seven days** after its creation; the rules compute it from the stored creation time, so no client clock enters the decision.

**Knowing the code is the authorization to read its invitation.** Any signed-in caller may `get` an invitation; nobody may `list` them. Members will list their own group's, with the revocation screen (see Consequences). A member may create an invitation for their group, and revoke one that is still pending.

## Context

[ADR-003](adr-003-persistence-and-sync.md) made redemption the one privileged operation and gave it to an owned service, because "security rules deny every client write to the group's member list. Without that denial the service is a formality." Where that service runs was left open, and [ADR-005](adr-005-kotlin-server.md) narrowed it to a long-running host without choosing one.

The MVP needs two people in one group before it needs anything else. On the path ADR-003 drew, that waits for a host, a deployment, Firebase Admin credentials in it, and token verification on every call — operations work that is not the product, for a single endpoint. Meanwhile [#16](https://github.com/cyrillrx/family-planner/issues/16) is blocked on the founder's membership, which ADR-004 could not place either: the rules denying the member list deny the founder too.

## Rationale

### Rules can hold the guarantees ADR-003 asked of the service

ADR-003 lists what the invitation must be: unguessable, single-use, expiring, revocable, validated together with the membership it grants, and enforced by something other than the client. Each holds under rules:

| Guarantee             | Held by |
|-----------------------|---|
| Unguessable           | The client draws the code from a secure random source, and the rules refuse to create one that is not 22 base64url characters; `list` is denied, so no stranger enumerates codes. The randomness rests on a member's client, which PRD-001 trusts: the invitation guards against the stranger, who never chooses a code |
| Single-use            | The update requires a pending invitation; the second transaction finds it redeemed |
| Expiring              | Computed in the rule from the stored server timestamp |
| Revocable             | A revoked invitation fails the same check as a redeemed one |
| Atomic with the grant | `getAfter` ties the membership to the invitation within one transaction, in both directions |
| Not up to the client  | Rules run on Firestore's side and bypass nothing |

The denial ADR-003 insisted on is kept, only narrowed: a client still cannot write a membership on its own. It can create one only as half of a write whose other half is checked, and it never updates one. Deleting one — a member leaving, or removing another, as PRD-001 asks — opens with the feature that brings it, and stays denied until then. Deleting a group opens only with the last member's departure, which PRD-001 makes delete all of its data; until then it stays denied, since founding is anchored on the group not existing and its sub-collections would outlive it, letting a deleted group be founded again over its remaining members. Its pending invitations live outside it and are revoked with it, or one still valid would let someone into a group that no longer exists.

### It fails better than the service would

ADR-003 promised that "if the owned service is down, synchronization keeps working and the app is unaffected" — and redemption was the exception, since joining needed our service. Under rules, joining needs Firestore and nothing else, so the product's core path depends on no availability of ours at all.

It also shrinks the one unguarded place ADR-003 warned about: a service holding administrative credentials bypasses every rule, and a bug in it is unguarded. Redemption was the reason that service needed write access to the member list. It no longer does: sending notifications needs only to read, and its credential is granted no more.

### Addressing memberships by credential

[ADR-003](adr-003-persistence-and-sync.md) keeps our `UserId` apart from the Firebase UID so that identity survives leaving Firebase, and that is unchanged — the `UserId` is still on the membership. The address is a different matter: ADR-003 says "Firestore document paths are addressing, not identity", and the users collection is already addressed by credential. Addressing memberships the same way is what lets a rule check membership with one `exists()`, without trusting a `UserId` the user document lets anyone claim.

[ADR-007](adr-007-account-linking.md)'s linking keeps the UID, so a member who signs in keeps their membership's address. One who signs in to an existing account takes that account's UID, and finds that account's memberships.

## Consequences

- **`server/` has no endpoint on the MVP's path.** Its hosting stops blocking the group, and becomes a question for the notifications.
- **The rules carry logic, against ADR-003's "authorization expressed as rules, not as logic".** Redemption and founding are decisions — the two ways into a group — and they now live in a language `jvmTest` does not reach. The domain stays the place for everything else, and the membership rules are the one exception, small and named here.
- **The rules need tests of their own.** The Firestore emulator runs them; nothing in CI does yet. Until it does, they are verified by hand against a project, and a regression there opens the group to whoever holds an old code.
- **The expiry is written twice.** The rules enforce seven days and the client shows when a pending invitation expires. The rule is the authority; the client's value is a display, and a drift between them shows a wrong date, not a wrong decision.
- **The code is a document address.** It appears in the Firebase console and in Firestore's own request logs, which ADR-003's "never written to any log" did not contemplate. Those are ours to read and no one else's; it still never reaches an application log, an analytics event or a crash report.
- **A collection-group index is deployed beside the rules**, and goes out with them ([#62](https://github.com/cyrillrx/family-planner/issues/62)).
- **Redeeming needs the network**, as it did under ADR-003. A batch would be queued offline and applied to the local cache, then refused later with nobody watching; a transaction fails at once instead, which is why redemption is one.
- **An invitation's link is shown once the server has acknowledged it.** A code shared before it exists there would read as missing to the invitee, so creating an invitation needs the network, like redeeming it.
- **Founding a group works offline.** Its batch is queued like any other write, and nothing it checks is contested by anyone else, so a later refusal is a client bug rather than a race. Asking for the network to create a group would cost the member more than it protects.
- **The rules do not hold a credential to one group.** Belonging to several groups is where the product is going, and each membership is still earned by its own invitation or founding, so a second one exposes nothing. One group per member is a V1 limit the client keeps, by offering neither joining nor founding to a member; [#17](https://github.com/cyrillrx/family-planner/issues/17) and [#19](https://github.com/cyrillrx/family-planner/issues/19) are the windows where it does not yet, and no longer the barrier ADR-005 looked for in the service.
- **A member who joins can read the group's invitations by code only**, like anyone. Listing the group's pending invitations, for revocation, is a query scoped by group that the rules allow to members — added with the screen that needs it.

## Alternatives considered

**Hosting `server/` now, as ADR-003 planned** — Rejected for the MVP: a host, a deployment and administrative credentials for one endpoint the rules can replace, and the core path tied to our availability. It stays the route if redemption ever needs something rules cannot express — rate limiting, or a side effect outside Firestore.

**A Cloud Function triggered on redemption** — Rejected: it needs the Blaze plan, and it reintroduces a privileged writer that bypasses the rules, which is the surface this decision removes.

**Memberships addressed by `UserId`** — Rejected: every rule would have to read the user document to map the caller's UID to a `UserId`, and would then trust a value [#68](https://github.com/cyrillrx/family-planner/issues/68) shows anyone can claim.

**The code's hash as the document address** — Rejected for now: it keeps the code out of the console, at the cost of hashing on the client and in the rules. Worth revisiting if the console ever becomes shared.

## Open Questions

- **How does a member read another member's name?** [ADR-004](adr-004-user-identity-and-membership.md) scopes `/users` reads by shared membership, but `users/{credential}` names no group, so a rule cannot test it. Listing a user's groups on their user document makes the rule expressible; copying the name onto the membership would too, but reopens what ADR-004 rejected; decided with [#77](https://github.com/cyrillrx/family-planner/issues/77).
- **Does the Desktop SDK carry this?** `runTransaction`, server timestamps and collection-group queries have to work through `firebase-java-sdk`, which [ADR-007](adr-007-account-linking.md) found missing methods in. Checked with [#77](https://github.com/cyrillrx/family-planner/issues/77), before the store depends on them.
- **How are the rules tested in CI?** The emulator runs through the Firebase CLI, which needs Node and a JDK on the runner; whether that runs in the existing workflow or beside it is decided with the first rules test.
