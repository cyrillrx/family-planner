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

The first three are [PRD-001](prd/prd-001-group-and-synchronization.md) Phase 2. Only device-to-device sync depends on authentication; group teardown does not, and could land earlier.

- [ ] Authentication, which is what makes an identity survive a lost phone
- [ ] Synchronization between one member's own devices
- [ ] Leaving a group, removing a member, deleting an empty group
- [ ] Basic fridge stock management
- [ ] Missing ingredient detection → auto grocery list
- [ ] Stock-based meal suggestions

## V3 — Nice to have

- [ ] Local generation with Gemma (high-end Android + iPhone)
- [ ] Expiration date tracking
- [ ] iOS / Android home screen widget for grocery list
