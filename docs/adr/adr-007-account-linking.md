# ADR-007: Members link their anonymous identity to Google or Apple

> **Status**: Accepted | **Date**: 2026-10-23 | **Context**: Firebase now deletes an anonymous account 30 days after its creation, and the anonymous account is a member's only identity ([#73](https://github.com/cyrillrx/family-planner/issues/73)).

## Decision

**A member keeps their identity by linking their anonymous account to Google or Apple.** Linking adds a credential to the account and keeps its UID, so the user document, the membership and the attribution of every record stay where they are. Nothing migrates.

| Provider | Offered on                                                                   |
|----------|------------------------------------------------------------------------------|
| Google   | Android, Desktop, iOS                                                        |
| Apple    | iOS, from the release that offers Google there — the App Store requires both |

No password, on any platform.

**The mechanism differs per platform, behind one store.** Per [ADR-006](adr-006-firestore-behind-stores.md), the repository sees an operation that links a credential and answers with the linked user; the adapter is the only code that knows how.

| Platform | Obtaining the credential                                      | Linking                                                 |
|----------|---------------------------------------------------------------|---------------------------------------------------------|
| Android  | Credential Manager, yielding a Google ID token                | `linkWithCredential` through the SDK                    |
| iOS      | The native Google and Apple flows, once [#47](https://github.com/cyrillrx/family-planner/issues/47) links Firebase | `linkWithCredential` through the SDK |
| Desktop  | The system browser, an OAuth client of type *Desktop app*, a loopback redirect and PKCE | The Firebase Auth REST API, `accounts:signInWithIdp`, carrying the anonymous account's ID token |

**On Desktop the SDK is bypassed for this one call.** `firebase-java-sdk` 0.6.3, the latest release, throws `NotImplementedError` from `linkWithCredential`, `signInWithCredential` and the email-link methods. The REST endpoint links the provider to the account whose ID token it receives, which is what the JavaScript SDK does underneath. The SDK's session survives it, since the UID does not change.

**A credential already linked to another account is refused, not merged.** That is a member signing in from a second device, which is Phase 2 of [PRD-001](../prd/prd-001-group-and-synchronization.md) and needs more than linking — see the open questions.

The Desktop OAuth client's identifier and secret are read from `local.properties`, like the Firebase options. Google does not treat an installed application's secret as confidential, but the repository is public.

## Context

PRD-001 shipped Phase 1 without authentication and accepted the cost: an identity that does not survive losing the device. Firebase's clean-up of anonymous accounts turns that bound into a deadline — 30 days after installing, every member would lose their identity, not only the ones who lose a phone. The clean-up was switched on deliberately, with authentication brought forward to V0 on the roadmap.

[ADR-003](adr-003-persistence-and-sync.md) chose anonymous sign-in because linking keeps the UID, and listed as unverified whether linking works through the JVM port. It does not through the SDK. Desktop is the platform the product is tested on day to day, so its answer decides whether the plan holds.

## Rationale

### Linking, not signing in

Signing in with Google creates or opens an account under a new UID. Every reference to the old one — the user document addressed by it, the security rules that grant access through it — would have to move. Linking leaves the UID untouched, which is the property ADR-003 picked Firebase Auth for.

### Verified on Desktop before deciding

A throwaway program ran the whole Desktop path against the real project: anonymous sign-in through the SDK, Google sign-in in the browser, linking through `accounts:signInWithIdp`. The endpoint answered with the same UID and `google.com` as provider, and the SDK refreshed its token for that UID afterwards. The Desktop OAuth client was accepted without being added to an allow-list, since it belongs to the same project.

### Google first, Apple where it is owed

Google covers every tester today and every platform. Apple is required on iOS by the App Store once another provider is offered there, and is not needed anywhere before that.

## Consequences

- **A linked account is out of reach of the 30-day clean-up.** An unlinked one is not: the prompt to link has to reach a member well before their account is 30 days old.
- **Desktop depends on a REST endpoint the SDK does not wrap.** It is the public, versioned Identity Toolkit API, and the adapter is the only code that knows it. If the SDK implements linking one day, the adapter changes and nothing else.
- **Desktop carries an OAuth flow of its own**: a loopback server, PKCE, a browser launch. It is adapter code, excluded from coverage like every adapter under ADR-006.
- **Linking needs the network.** It is the one step of the product that cannot be done offline, and the interface has to say so rather than queue it.

## Alternatives considered

**A one-time code sent by email** — Rejected for now: Firebase does not offer it, so `server/` would generate, send and verify the codes, carry a mail provider's secret, and defend against brute force itself.

**An email link** — Deferred: it is native to Firebase, and the fallback if a member has neither Google nor Apple. On Desktop it would go through the REST API too, since the SDK does not implement it.

**A password** — Rejected: one more secret for the member to remember and to reset, for no capability Google and Apple do not already give.

**Waiting for `firebase-java-sdk` to implement linking** — Rejected: nothing announces it, and the 30-day clock is already running.

**No linking on Desktop** — Rejected: Desktop is the platform the product is tested on, and its members would lose their identity on the same schedule as everyone else.

## Open Questions

- **How does a member open their existing account on a new device?** On Android and iOS, `signInWithCredential` does it. On Desktop the SDK cannot adopt a session it did not create, so the likely route is a custom token minted by `server/` after it verifies the provider's token — `signInWithCustomToken` is implemented on the JVM. Settled with PRD-001 Phase 2.
- **When is the member asked to link?** During onboarding adds a step to the first launch, which PRD-001 keeps to a name and one choice. Later needs a reminder that arrives before the 30 days run out. Belongs to the PRD or the screen that introduces it.
- **Is Apple offered outside iOS?** A member with an iPhone and a Desktop would need it on both to reach one account. Settled when iOS ships.
