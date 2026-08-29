# Kotlin Multiplatform & Compose Multiplatform Conventions

> [!IMPORTANT]
> **Canonical source of truth** (shared, project-agnostic): [`conventions/kmp-conventions.md`](https://github.com/cyrillrx/coding-conventions/blob/main/conventions/kmp-conventions.md) — do not duplicate here.

The full KMP / Compose Multiplatform conventions (MVVM + UDF, state & event modeling, navigation, lifecycle-aware refresh, Compose rules, naming, formatting, testing) live in the canonical document and apply as-is to the `cmp-app/` client.

## Project-specific additions

- **Module split** — `cmp-app/shared/core` holds the domain and data layers and has no Compose dependency; `cmp-app/shared/ui` holds the Compose layer. `androidApp`, `desktopApp` and `iosApp` are platform wrappers. The rationale is in [ADR-001](../adr/adr-001-kmp-client-targets.md).
- **Package** — `com.cyrillrx.family`, in both shared modules.
- **Test location** — tests live in each module's `src/commonTest/`, and run on the JVM target. `jvmTest` is what feeds coverage.
- **Targets** — Android, iOS and Desktop. No Web target; a library that does not support `wasmJs` is not disqualified today, but see ADR-001 for what that costs later.
- **Formatting** — ktlint, configured by the repository-root [`.editorconfig`](../../.editorconfig), itself copied from the shared conventions repository. Strict in `core`, permissive in `ui`.

Dependency injection, navigation and the design system are not settled yet — they get their bindings here as they are decided.
