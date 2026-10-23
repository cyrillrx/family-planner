# ADR-007: Members sign in with Google, linking their anonymous identity

> **Status**: Accepted | **Date**: 2026-10-23 | **Context**: Firebase now deletes an anonymous account 30 days after its creation, and the anonymous account is a member's only identity ([#73](https://github.com/cyrillrx/family-planner/issues/73)).

## Decision

**A member signs in with Google, from the first release.** Google is the only provider, on every platform. No password, and no Apple for now — see the consequences for what that defers.

Signing in takes one of two paths, and the member does not choose between them:

- **Link** when the Google account is new to the product. Google is added to the device's anonymous account, which keeps its UID: the user document, the membership and the attribution of every record stay where they are. Nothing migrates.
- **Sign in** when the Google account is already linked to another account — the member's own, from another device. The device opens that account instead, **without asking**, and its anonymous account is abandoned along with any membership it held. Firebase deletes it 30 days after its creation, like any anonymous account.

A member can therefore open their account on several devices. Each one reaches the same user document, addressed by the same UID.

**The mechanism differs per platform, behind one store.** Per [ADR-006](adr-006-firestore-behind-stores.md), the repository sees one operation that signs in with a Google credential and answers with the user it opened, linked or found; the adapter is the only code that knows how.

| Platform | Obtaining the credential                                   | Linking                                   | Signing in                                     |
|----------|------------------------------------------------------------|-------------------------------------------|------------------------------------------------|
| Android  | Credential Manager, yielding a Google ID token             | `linkWithCredential`                      | `signInWithCredential`, on a collision         |
| iOS      | The native Google flow, once [#47](https://github.com/cyrillrx/family-planner/issues/47) links Firebase | `linkWithCredential` | `signInWithCredential`, on a collision |
| Desktop  | The system browser, an OAuth client of type *Desktop app*, a loopback redirect and PKCE | REST `accounts:signInWithIdp`, carrying the anonymous ID token | REST `accounts:signInWithIdp` without it, on `FEDERATED_USER_ID_ALREADY_LINKED`, then the session handed to the SDK |

**On Desktop the SDK is bypassed for both calls.** `firebase-java-sdk` 0.6.3, the latest release, throws `NotImplementedError` from `linkWithCredential`, `signInWithCredential`, `updateCurrentUser` and the email-link methods. The Firebase Auth REST API does both, as the JavaScript SDK does underneath:

- After a **link**, the SDK's session survives untouched, since the UID does not change.
- After a **sign-in**, the SDK has to adopt a session it did not create. It reloads its user at start-up from the `FirebasePlatform` storage, which ours is, under the key `com.google.firebase.auth.FIREBASE_USER`. The adapter writes the REST response there — `uid`, `idToken`, `refreshToken`, `expiresIn`, `isAnonymous` — and re-initialises the Firebase application, which reads it back.

The Desktop OAuth client's identifier and secret are read from `local.properties`, like the Firebase options. Google does not treat an installed application's secret as confidential, but the repository is public.

## Context

[PRD-001](../prd/prd-001-group-and-synchronization.md) shipped Phase 1 without authentication and accepted the cost: an identity that does not survive losing the device. Firebase's clean-up of anonymous accounts turns that bound into a deadline — 30 days after installing, every member would lose their identity, not only the ones who lose a phone. The clean-up was switched on deliberately, with authentication brought forward to V0 on the roadmap.

Linking alone would save the identity on the device that holds it. It would not let a member who installs the product on a second device reach it, and a link attempted there fails: Google is already linked to the first account. Signing in is the answer to that failure, and it is what makes the identity worth saving.

[ADR-003](adr-003-persistence-and-sync.md) chose anonymous sign-in because linking keeps the UID, and listed as unverified whether linking works through the JVM port. It does not through the SDK. Desktop is the platform the product is tested on day to day, so its answer decides whether the plan holds.

## Rationale

### Link first, sign in on a collision

Signing in without trying to link first would open a new account under a new UID for every member who is new to the product. Every reference to their anonymous account — the user document addressed by it, the security rules that grant access through it — would have to move. Linking first leaves the UID untouched whenever it can, and the collision says precisely when it cannot.

### Transparent, at the cost of what the device held

Asking before switching would put a decision in front of a member who has just said which account they want, which PRD-001's low-cognitive-load requirement argues against. What the switch abandons is bounded: a membership held by the anonymous account, which the member can be invited back to from the account they chose.

### Verified on Desktop before deciding

Two throwaway programs ran the Desktop paths against the real project.

- **Linking:** anonymous sign-in through the SDK, Google sign-in in the browser, `accounts:signInWithIdp` with the anonymous ID token. Same UID back, `google.com` as provider, and the SDK refreshed its token for that UID afterwards.
- **Signing in from a fresh device:** an empty storage, a new anonymous account, a link refused with `FEDERATED_USER_ID_ALREADY_LINKED`, then `accounts:signInWithIdp` without an ID token. The session written to the storage and the application re-initialised, the SDK reported the existing UID as not anonymous, refreshed its token, and Firestore granted the read the rules reserve to that UID.

The Desktop OAuth client was accepted without being added to an allow-list, since it belongs to the same project.

### No server for the Desktop sign-in

A custom token minted by `server/` after verifying the Google token would use `signInWithCustomToken`, which the JVM SDK implements. It is rejected for now: it puts a deployed service on the path of every Desktop sign-in, and [ADR-005](adr-005-kotlin-server.md) has not settled where the service runs.

### Google alone

Google covers every tester today and every platform. A second provider is a second flow per platform to build and to verify on Desktop, for members who do not exist yet.

## Consequences

- **A linked account is out of reach of the 30-day clean-up.** An unlinked one is not: the prompt to sign in has to reach a member well before their account is 30 days old.
- **One member, several devices.** PRD-001's "one device per user" no longer holds, and the requirements filed under its Phase 2 for it move forward with this decision.
- **The Desktop sign-in depends on the SDK's stored session format**, which is the SDK's own and undocumented. The adapter is the only code that writes it, and a `jvmTest` that hands the SDK a stored session and reads its user back, without a network, turns an incompatible SDK upgrade into a failing build rather than a failing sign-in.
- **Desktop depends on a REST endpoint the SDK does not wrap.** It is the public, versioned Identity Toolkit API. If the SDK implements these calls one day, the adapter changes and nothing else.
- **Desktop carries an OAuth flow of its own**: a loopback server, PKCE, a browser launch. It is adapter code, excluded from coverage like every adapter under ADR-006.
- **A switch abandons the device's anonymous membership without a word.** A member who joined a group without signing in, then signs in to an account that belongs elsewhere, leaves that group. The anonymous membership stays in the group's member list until a member is removed — PRD-001 Phase 2.
- **iOS cannot ship with Google alone.** The App Store requires Sign in with Apple, or an equivalent, from an application that offers a third-party sign-in. Apple joins as a provider before the first iOS release, with its own Desktop verification.
- **Signing in needs the network.** It is the one step of the product that cannot be done offline, and the interface has to say so rather than queue it.

## Alternatives considered

**Asking before switching to an existing account** — Rejected: it puts a second decision in front of a member who has just made it.

**A custom token minted by `server/` for the Desktop sign-in** — Deferred: it removes the dependency on the SDK's stored session, and is the route if that format becomes a burden, but needs a deployed service.

**A one-time code sent by email** — Rejected for now: Firebase does not offer it, so `server/` would generate, send and verify the codes, carry a mail provider's secret, and defend against brute force itself.

**An email link** — Deferred: it is native to Firebase, and the fallback if a member has no Google account. On Desktop it would go through the REST API too, since the SDK does not implement it.

**Apple alongside Google from the start** — Deferred: no tester needs it, and the App Store only requires it once iOS ships.

**A password** — Rejected: one more secret for the member to remember and to reset, for no capability Google does not already give.

**Waiting for `firebase-java-sdk` to implement these calls** — Rejected: nothing announces it, and the 30-day clock is already running.

**No sign-in on Desktop** — Rejected: Desktop is the platform the product is tested on, and its members would lose their identity on the same schedule as everyone else.

## Open Questions

- ~~**How does a member open their existing account on a new device?**~~ Settled above: by signing in with Google, through the stored session on Desktop.
- ~~**When is the member asked to sign in?**~~ Settled: the first screen offers it beside starting without an account, and an optional step closes the anonymous onboarding, which PRD-001 keeps to a name and one choice. A reminder stays on the home screen until the account is linked, so putting it off does not run out the 30 days unnoticed.
- **Is Apple offered outside iOS, once it arrives?** A member with an iPhone and a Desktop would need it on both to reach one account. Settled when iOS ships.
