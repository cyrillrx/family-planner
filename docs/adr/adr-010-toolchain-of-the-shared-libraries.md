# ADR-010: The project builds on the toolchain of the libraries it shares

**Status**: Accepted  
**Date**: 2026-11-22  
**Amends**: [ADR-001](adr-001-kmp-client-targets.md)  
**Context**: [#38](https://github.com/cyrillrx/family-planner/issues/38) adopts `io.github.cyrillrx:logger`, the first library this project consumes from [`kmp-logger-tracker`](https://github.com/cyrillrx/kmp-logger-tracker). Its 0.1.0 ships JVM bytecode 25, requires `kotlin-stdlib` 2.4.21 and an Android `compileSdk` of 37. On Java 21, every test that loads it fails with `UnsupportedClassVersionError`.

## Decision

**The project builds on JDK 25**: the Gradle daemon, CI, the JVM targets of the client and `server/`. ADR-001 said "Java 21 across the board"; read it as superseded on that point.

**The bytecode Android consumes stays at 21.** The Android application, the Android targets of `app/shared`, and `core:model`, whose JVM variant Android reads ([ADR-005](adr-005-kotlin-server.md)), keep `jvmTarget` 21, the level the library also publishes its Android variant at.

**A shared library sets the floor, and the project follows it.** `kmp-logger-tracker` builds with the latest stable toolchain, and its own ADR-001 asks a consumer to align before adopting a release rather than have the library level down. Kotlin, `compileSdk` and the JDK move here when a release of a library requires it.

## Rationale

**The library is the one that serves several projects.** Pinning it to the oldest consumer would freeze it on whichever project was touched last. The cost of aligning falls on a consumer once per upgrade, and is a version bump, not a code change.

**One JDK for the whole build.** `server/` does not depend on the logger, but keeping it on 21 would leave two toolchains in one Gradle build and one CI workflow, for no gain.

## Consequences

- **Desktop needs a Java 25 runtime**, which the packaged application bundles, and the host runs `server/` on Java 25.
- **Adopting a release of a shared library can bring a toolchain upgrade first**, in its own commit, before the dependency.
- **Android is not affected** beyond `compileSdk`: `minSdk` and `targetSdk` are unchanged, and the bytecode it dexes stays at 21.

## Alternatives considered

**Ask the library to publish JVM bytecode 21.** Rejected: it contradicts the library's own decision, and only defers the alignment to the next release that needs a newer JDK.

**Move the client to 25 and keep `server/` on 21.** Rejected: two toolchains in one build, for a service that has no reason to stay behind.
