# PRD-001 — Group and synchronization

> **Status**: Approved | **Version**: 0.5 | **Last updated**: 2026-10-23

## Overview

Every piece of data in Family Planner belongs to a **group**. A group may be one person, two adults, or a whole family — the product never assumes a size. This document defines what a group is, how someone joins one, which data is shared inside it and which stays personal, and what the app promises when several devices change the same thing.

It is a foundation document rather than a feature: the meal planner, the shared events and the shared task list all sit on top of it, and each of them will get its own PRD. Sync is also the part of the product that cannot be retrofitted — a feature written against a single-device assumption has to be rewritten, not extended.

**Phase 1 delivers synchronization between members, and between one member's devices once they sign in.** A member can start without an account, on one device, and is offered to sign in from the first screen. Signing in is what makes an identity survive a lost phone and reach a second device. Version 0.5 brought it forward from Phase 2: the anonymous identity a member starts with is now deleted 30 days after the first launch, so an account is no longer a convenience that can wait.

This PRD describes behaviour only. It does not pick a database, a sync engine or an authentication provider: those are a technical decision, and the requirements below are what the ADR that follows has to satisfy. The client platform is already settled in [ADR-001](../adr/adr-001-kmp-client-targets.md).

## Goals

- A change made by one member is visible to the others without anyone refreshing, retrying or wondering whether it went through.
- The app is fully usable with no network, and nothing entered offline is lost for as long as the app stays installed.
- A group of one works exactly like a group of five: no feature is hidden, degraded, or waiting on a second member to arrive.
- Getting started asks for the minimum — a name to be known by, and one choice — and never more.
- Two devices changing the same thing at once produce a predictable result, never a silently discarded edit.
- Nothing in the model prevents a member from belonging to several groups later, even though only one group is supported now.

## User Stories

### Phase 1

| As a…          | I want to…                                | So that…                                                |
|----------------|-------------------------------------------|---------------------------------------------------------|
| Person alone   | use the app without inviting anyone       | it is useful before it is shared                        |
| Group founder  | invite someone by sharing a link or code  | we both see and edit the same data                      |
| Invited person | join the group on my very first launch    | I land in the shared data instead of my own empty group |
| Member         | see a change another member just made     | we do not both buy the milk                             |
| Member         | add a task while I have no network        | I do not have to hold it in my head until I get signal  |
| Member         | know when what I am looking at may be old | I can trust the screen when it tells me it is current   |
| Member         | sign in on a new device                   | I find my group and my data there                       |
| Member         | find my own edits on my other device      | I never have to remember which device I used            |

### Phase 2

| As a…        | I want to…                           | So that…                                     |
|--------------|--------------------------------------|----------------------------------------------|
| Member       | leave the group                      | I am not tied to it forever                  |
| Group member | remove someone who no longer belongs | our data stops being visible to them         |
| Member       | see who changed something, and when  | I can ask the right person about it          |

## Functional Requirements

### Phase 1 — MVP

**Group lifecycle**

- [ ] The first launch offers to sign in. Without signing in, it asks for a display name and one explicit choice: **create a new group**, or **join an existing one** with an invitation.
- [ ] The app never creates a group silently. An invited member who lands in a group of their own has to be extracted from it, and there is no simple way to do that — so the choice is asked for rather than guessed.
- [ ] Joining requires a valid invitation. Without one, creating a group is the only way forward.
- [ ] The group has a stable identifier, and every shared record carries it.
- [ ] The group has a generated default name, displayed nowhere in V1. It becomes editable if multi-group arrives and a group needs to be told apart from another.
- [ ] A group with a single member behaves identically to a group with several: no feature is hidden or degraded.

**Membership**

A *user* is a person's identity and their display name; a *membership* is that user's place in a group. "Member" below means a user seen through their membership. The two are separate in the model — see [ADR-004](../adr/adr-004-user-identity-and-membership.md).

- [ ] A member can produce an invitation that another person redeems to join the group.
- [ ] An invitation can be revoked before it is used, and stops working after it expires.
- [ ] All members hold the same rights over shared data. There are no roles and no owner privileges in V1.
- [ ] Any member can create an invitation, and redeeming one grants that same right in turn. With no roles there is no owner to reserve it for. This is reopened the day roles arrive.
- [ ] A user starts with an identifier generated locally on their device, without an account. Signing in is offered and never required to use the app.
- [ ] A user supplies a display name. It is the only thing about them the other members see.
- [ ] Signing in attaches the user to an account. A user new to the product keeps their identifier and their place. A user whose account already exists, from another device, opens it instead — without being asked, since they just chose it — and what the device held without an account is abandoned.
- [ ] A signed-in user is one identity across all of their devices. Adding a device does not create a second member.
- [ ] A user who has not signed in is one device. Their identity does not survive uninstalling the app, losing the device, or 30 days after their first launch; it cannot be recovered, and they have to be invited again. Until they sign in, the app reminds them.

**Shared and personal data**

- [ ] Shared across the group: the week plan, the grocery list, the events and the tasks.
- [ ] Personal to a member: their notification preferences and their device's display settings.
- [ ] A personal setting never propagates to another member, and never appears in another member's view.

**Synchronization**

- [ ] Every shared record carries its group, the moment it was last changed, and who changed it.
- [ ] A change made on one device reaches the group's other connected devices without a manual refresh.
- [ ] A change made offline applies locally at once, and propagates on its own when connectivity returns — without the member re-entering it or confirming anything.
- [ ] Deleting a record propagates as an explicit deletion, so a device that was offline when it happened does not bring the record back.
- [ ] The app shows whether what is on screen is current or waiting to sync, without making the member interpret it.

**Concurrent changes**

- [ ] Two members checking the same grocery item at the same moment converge to the same state on every device.
- [ ] Two members editing different fields of the same record both keep their change, where the data allows it.
- [ ] When two changes genuinely cannot be merged, the winner is decided by a rule that gives the same result on every device, whatever order the changes arrive in.
- [ ] A change that loses a conflict is never dropped without the member being able to tell: the app either keeps the losing value or says that it was replaced.
- [ ] **The last write wins, on server time.** A deletion that lands after an edit removes the record; an edit that lands after a deletion is applied to nothing. This is what the store does natively, and adopting it rather than overriding it means there is no conflict layer of our own to write, tune and test. See [ADR-003](../adr/adr-003-persistence-and-sync.md).
- [ ] The cost is accepted and named: a member can lose an edit to a deletion they never saw. It is bounded by the same seven-day window as everything else — a device cannot lose an edit to a deletion older than the reconciliation period.

### Phase 2

- [ ] A member can leave the group. The records they authored stay, and remain attributed to them.
- [ ] When the **last** member leaves, the group and all of its shared data are deleted. No orphan group is left behind.
- [ ] Leaving as the only member is therefore the same act as deleting the group. The app says so before it happens, because it cannot be undone.
- [ ] A member can be removed from the group by another member, and their device loses access to its data. This is also how a group gets rid of a member whose device is gone for good: the remaining members remove the ghost, and the group can reach empty and be deleted like any other.
- [ ] The group survives the departure of the person who created it. There is no owner privilege to inherit.
- [ ] A shared record shows its author and its last change in the UI.

## Non-Functional Requirements

- **Offline-first.** Every read is served from local storage. The app opens, displays data and accepts changes with the network off, on a cold start, with no error state and no blocking spinner.
- **Durability before confirmation.** A change is written locally before the UI reports it as done. Nothing that the interface confirmed may disappear on a crash or a force-quit.
- **The bound on durability.** That guarantee covers a live installation. For a member who has not signed in, it does not extend to uninstalling the app, losing the device, or the 30 days after which their anonymous identity is deleted: with no account behind it, their identity lives only on their device. In a group of several, the loss costs that member their place, and the others remove the ghost and carry on with the shared data intact. In a **group of one**, it costs everything — nobody is left to remove anyone, nobody is left to leave, and the group's data becomes permanently unreachable. That is why signing in is offered from the first screen and recalled until it happens.
- **Perceived immediacy.** A change made by one member appears on another connected member's device within a few seconds — the grocery list is used by two people in the same shop at the same time.
- **Group isolation.** Group scoping is enforced when data is read, not only when it is written. A device never receives records belonging to another group.
- **Offline window: seven days.** A device that has been offline for up to seven days reconciles: everything it did while away is sent, and everything it missed is received, the last-write-wins rule above deciding any collision. Deletions stay knowable for at least that long. Past the window, the device recovers by reloading the group's whole state rather than a delta, with its own unsent changes replayed on top — slower, but never a failure the member has to act on.
- **Trust inside, a lock on the door.** The guarantees above are upheld by the client. Members are trusted, so no server-side enforcement defends the data against one of them. The **invitation is the exception**, because it is the boundary rather than the inside: it must be unguessable, revocable and expiring, and its redemption is the one thing that cannot rest on a well-behaved client. This is not about a hostile member, it is about a stranger at the door.
- **Size independence.** No requirement, screen or data structure assumes a number of members, an upper bound, or a relationship between them.
- **Shared implementation.** Sync behaviour comes from the shared Kotlin code and is identical on Android and iOS, per [ADR-001](../adr/adr-001-kmp-client-targets.md). Anything a platform cannot honour is a constraint on the design, not a per-platform variation.
- **Testable without a device pair.** Conflict and offline behaviour must be verifiable in the shared Kotlin tests, without two phones and without a network — otherwise the guarantees above cannot be part of CI.

## Out of Scope

- **Multi-group membership.** The model must keep it possible, but nothing here implements it. It gets its own PRD if and when it is needed.
- **Roles and permissions.** All members are equal in V1. A read-only or child account is a later product question.
- **The technology.** Local storage, sync transport, conflict mechanics and authentication provider are decided in a following ADR, not here.
- **The choice of providers.** Which ones are offered, and how signing in works on each platform, is [ADR-007](../adr/adr-007-account-linking.md)'s. This document requires that signing in exists, stays optional, and keeps the identity of a member new to the product.
- **Notification delivery.** Telegram messages and their scheduling are a separate concern, covered when `server/` gets its PRD.
- **What the shared data means.** The semantics of a week plan, an event or a task belong to the meal, events and tasks PRDs. This document only says they are shared.
- **Hostile members.** The group is made of people who trust each other. Defending the data against one of its own members is not a V1 requirement — which is what makes client-side enforcement acceptable. It says nothing about who is kept out, and the invitation still has to hold.

## Open Questions

The seven questions this document opened at version 0.1 are settled in the requirements above. What remains are the holes those answers created.

- Should a group with no active device for a long time be cleaned up? It is the only possible recourse against the group of one whose only member uninstalls — nobody can leave it and nobody can be removed from it, so it can never delete itself.
- ~~When authentication arrives, how does it attach an **existing anonymous user** to an account without losing their local data?~~ Settled by [ADR-007](../adr/adr-007-account-linking.md): signing in links the account to the anonymous user, which keeps its identifier.
- ~~What does a resurrected record look like, per feature?~~ Moot since [ADR-003](../adr/adr-003-persistence-and-sync.md): with the last write winning, a deletion is never undone by a concurrent edit, and a device returning after the window adopts the group's state wholesale. Nothing resurrects. Kept here because the question was real under v0.2's rule.
- Is the seven-day offline window fixed, or does it need to be configurable?
- Is the recipe library shared across the group, or personal to a member? The shared list above names the week plan, the grocery list, the events and the tasks — recipes are absent, which reads more like an oversight than a decision. It also settles whether any local store beyond the sync cache is needed. Belongs to the meal PRD.
