# Kotlin Conventions

> [!IMPORTANT]
> **Canonical source of truth** (shared, project-agnostic): [`conventions/kotlin-conventions.md`](https://github.com/cyrillrx/coding-conventions/blob/main/conventions/kotlin-conventions.md) — do not duplicate here.

The Kotlin conventions (naming, formatting, idioms, testing, multiplatform) live in the canonical document and apply as-is to every module. The UI layer of the `app/` client also follows the [Compose conventions](compose-conventions.md).

## Project-specific additions

- **Module split** — `core/model` holds the entities and the wire types, shared with the server and depending on nothing; `app/shared/domain` holds the repositories and use cases and has no Compose dependency; `app/shared/ui` holds the Compose layer. `androidApp`, `desktopApp` and `iosApp` are platform wrappers. The rationale is in [ADR-001](../adr/adr-001-kmp-client-targets.md).
- **Package** — `com.cyrillrx.family` for everything specific to this product, in both shared modules. Types that carry nothing of the product — `Result`, `Error`, `ApiResponse` — live under `com.cyrillrx.core`, the package `kmp-ttrpg-companion` uses for the same purpose, so they stay copyable between projects and extractable into a library later.
- **Test location** — tests live in each module's `src/commonTest/`, and run on the JVM target. `jvmTest` is what feeds coverage.
- **Targets** — Android, iOS and Desktop. No Web target; a library that does not support `wasmJs` is not disqualified today, but see ADR-001 for what that costs later.
- **Formatting** — ktlint, configured by the repository-root [`.editorconfig`](../../.editorconfig), itself copied from the shared conventions repository. Strict in `core`, permissive in `ui`.

Dependency injection, navigation and the design system are not settled yet — they get their bindings here as they are decided.
