# PRD-001 — Group and synchronization

> **Status**: Draft | **Version**: 0.1 | **Last updated**: 2026-09-01

## Overview

Every piece of data in Family Planner belongs to a **group**. A group may be one person, two adults, or a whole family — the product never assumes a size. This document defines what a group is, how someone joins one, which data is shared inside it and which stays personal, and what the app promises when several devices change the same thing.

It is a foundation document rather than a feature: the meal planner, the shared events and the shared task list all sit on top of it, and each of them will get its own PRD. Sync is also the part of the product that cannot be retrofitted — a feature written against a single-device assumption has to be rewritten, not extended.

This PRD describes behaviour only. It does not pick a database, a sync engine or an authentication provider: those are a technical decision, to be recorded in a following ADR once the requirements below are agreed. The client platform is already settled in [ADR-001](../adr/adr-001-kmp-client-targets.md).

## Goals

- A change made by one member is visible to the others without anyone refreshing, retrying or wondering whether it went through.
- The app is fully usable with no network, and nothing entered offline is ever lost.
- A group of one works exactly like a group of five, with no extra setup and no dormant "invite people" step in the way.
- Two devices changing the same thing at once produce a predictable result, never a silently discarded edit.
- Nothing in the model prevents a member from belonging to several groups later, even though only one group is supported now.

## User Stories

### Phase 1

| As a…         | I want to…                                | So that…                                               |
|---------------|-------------------------------------------|--------------------------------------------------------|
| Person alone  | use the app without inviting anyone       | it is useful before it is shared                       |
| Group founder | invite someone by sharing a link or code  | we both see and edit the same data                     |
| Member        | find my own edits on my other device      | I never have to remember which device I used           |
| Member        | see a change another member just made     | we do not both buy the milk                            |
| Member        | add a task while I have no network        | I do not have to hold it in my head until I get signal |
| Member        | know when what I am looking at may be old | I can trust the screen when it tells me it is current  |

### Phase 2

| As a…        | I want to…                           | So that…                             |
|--------------|--------------------------------------|--------------------------------------|
| Member       | leave the group                      | I am not tied to it forever          |
| Group member | remove someone who no longer belongs | our data stops being visible to them |
| Member       | see who changed something, and when  | I can ask the right person about it  |

## Functional Requirements

### Phase 1 — MVP

**Group lifecycle**

- [ ] A group exists from the first launch, created without a dedicated setup screen — the app is usable before anything is shared.
- [ ] The group has a stable identifier, and every shared record carries it.
- [ ] A group with a single member behaves identically to a group with several: no feature is hidden or degraded.

**Membership**

- [ ] A member can produce an invitation that another person redeems to join the group.
- [ ] An invitation can be revoked before it is used, and stops working after it expires.
- [ ] All members hold the same rights over shared data. There are no roles and no owner privileges in V1.
- [ ] A member is one identity across all of their own devices — adding a second device does not create a second member.

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

### Phase 2

- [ ] A member can leave the group. The records they authored stay, and remain attributed to them.
- [ ] A member can be removed from the group, and their devices lose access to its data.
- [ ] The group survives the departure of the person who created it.
- [ ] A shared record shows its author and its last change in the UI.

## Non-Functional Requirements

- **Offline-first.** Every read is served from local storage. The app opens, displays data and accepts changes with the network off, on a cold start, with no error state and no blocking spinner.
- **Durability before confirmation.** A change is written locally before the UI reports it as done. Nothing that the interface confirmed may disappear on a crash or a force-quit.
- **Perceived immediacy.** A change made by one member appears on another connected member's device within a few seconds — the grocery list is used by two people in the same shop at the same time.
- **Group isolation.** Group scoping is enforced when data is read, not only when it is written. A device never receives records belonging to another group.
- **Size independence.** No requirement, screen or data structure assumes a number of members, an upper bound, or a relationship between them.
- **Shared implementation.** Sync behaviour comes from the shared Kotlin code and is identical on Android and iOS, per [ADR-001](../adr/adr-001-kmp-client-targets.md). Anything a platform cannot honour is a constraint on the design, not a per-platform variation.
- **Testable without a device pair.** Conflict and offline behaviour must be verifiable in `shared/core` tests, without two phones and without a network — otherwise the guarantees above cannot be part of CI.

## Out of Scope

- **Multi-group membership.** The model must keep it possible, but nothing here implements it. It gets its own PRD if and when it is needed.
- **Roles and permissions.** All members are equal in V1. A read-only or child account is a later product question.
- **The technology.** Local storage, sync transport, conflict mechanics and authentication provider are decided in a following ADR, not here.
- **How a member authenticates.** The requirement that a member is one identity across devices is stated above; the mechanism that establishes it belongs to the same ADR.
- **Notification delivery.** Telegram messages and their scheduling are a separate concern, covered when `telegram-bot/` gets its PRD.
- **What the shared data means.** The semantics of a week plan, an event or a task belong to the meal, events and tasks PRDs. This document only says they are shared.
- **Hostile members.** The group is made of people who trust each other. Defending the data against one of its own members is not a V1 requirement.

## Open Questions

- Does an edit win over a concurrent deletion, or the reverse? The answer differs by data: a resurrected grocery item is harmless, a resurrected event is not.
- Is the first launch anonymous with an upgrade path to an account, or is an account required before the app opens? This decides how much friction sits in front of a first-time user, and whether local data has to survive an account being attached later.
- What identifies a member across their devices before authentication exists, and does that identity survive a reinstall?
- Does redeeming an invitation grant the right to invite others in turn?
- How long must a device be able to stay offline before it is allowed to fail rather than reconcile? This sets how long explicit deletions have to be retained.
- Which of these guarantees need server-side enforcement, and which can trust the client, given the "no hostile members" assumption above?
- Does the group need a name, or is it invisible plumbing for as long as there is only one?
