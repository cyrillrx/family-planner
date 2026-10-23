# Roadmap

Planned phases for Family Planner. Scope is indicative, and the ordering matters more than the contents: V0 is what every later item stands on.

Technologies are deliberately absent from the items below. Persistence, sync, authentication and notification scheduling are still candidates — see [`draft-spec.md`](draft-spec.md) — and naming one here would decide it by accident.

## V0 — Foundation

The group and its synchronization — [PRD-001](prd/prd-001-group-and-synchronization.md), Phase 1. Nothing in V1 works without it: every feature below stores group-scoped data that has to reach the other members.

- [ ] Onboarding: a display name, and the choice between creating a group and joining one
- [ ] Group, membership and invitations
- [ ] Local persistence, and reads served from it
- [ ] Synchronization between the members of a group
- [ ] Conflict and deletion behaviour, verifiable without a network
- [ ] Signing in, linked to the anonymous identity — brought forward from V2, because Firebase deletes an anonymous account 30 days after its creation and a tester would lose their place in the group ([#73](https://github.com/cyrillrx/family-planner/issues/73))
- [ ] Synchronization between one member's own devices, which signing in brings with it

## V1 — Must have

- [ ] Meal planning + AI generation + manual validation
- [ ] Recipe detail screen
- [ ] Shared grocery list
- [ ] Recipe library
- [ ] Telegram notifications morning + evening
- [ ] Family events (recurring + one-off)
- [ ] Event reminders via Telegram
- [ ] Shared to-do list

## V2 — Should have

The first one is what remains of [PRD-001](prd/prd-001-group-and-synchronization.md) Phase 2, whose sign-in and device-to-device sync moved to V0. It does not depend on them, and could land earlier.

- [ ] Leaving a group, removing a member, deleting an empty group
- [ ] Basic fridge stock management
- [ ] Missing ingredient detection → auto grocery list
- [ ] Stock-based meal suggestions

## V3 — Nice to have

- [ ] Local generation with Gemma (high-end Android + iPhone)
- [ ] Expiration date tracking
- [ ] iOS / Android home screen widget for grocery list

## Cross-cutting

Outside the phases, because they are not features and they do not wait for one. The ordering above does not apply to them; they are listed so they are not rediscovered as emergencies.

- [ ] Somewhere for a failure to go — a caught exception is discarded today, and nothing records which one happened
- [ ] Crash reporting from the surfaces that ship, with the consent that any telemetry beyond it requires
- [ ] A product name that fits a group of any size — "Family Planner" assumes a family. Candidates lean towards Majordome, Butler or something close, and none is settled. The Android package is fixed once the app is on the Play Store, so it is decided before the first release
