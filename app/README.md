# app

Kotlin Multiplatform client for Family Planner, targeting Android, iOS and Desktop with a shared Compose Multiplatform UI.

## Modules

| Module        | Contains                                                                                                      |
|---------------|---------------------------------------------------------------------------------------------------------------|
| `shared/domain` | Repositories, use cases and error taxonomies. No Compose dependency.                                   |
| `shared/ui`   | Compose UI. Ships to iOS as the `Shared` framework.                                                           |
| `androidApp`  | Android application wrapper.                                                                                  |
| `desktopApp`  | JVM application wrapper. Development target — it runs the tests and produces coverage, it is not distributed. |
| `iosApp`      | Xcode project. Its build phase calls `:app:shared:ui:embedAndSignAppleFrameworkForXcode`.                 |

The entities and the wire types live one level up, in `core/model`, because the server depends on them too ([ADR-005](../docs/adr/adr-005-kotlin-server.md)).

The reasoning behind this split, and behind the absence of a Web target, is in [ADR-001](../docs/adr/adr-001-kmp-client-targets.md).

## Commands

Run from the repository root — the Gradle build lives there.

```bash
./gradlew build                          # Build every target
./gradlew jvmTest                        # Run the JVM tests
./gradlew koverXmlReport                 # Generate the aggregated report SonarCloud reads
./gradlew ktlintCheck                    # Check formatting
./gradlew ktlintFormat                   # Auto-fix formatting
./gradlew :app:desktopApp:run            # Run on Desktop
./gradlew :app:androidApp:installDebug   # Install on Android
```

iOS builds from `iosApp/iosApp.xcodeproj` in Xcode.

Contribution rules, including the coverage policy, are in [`AGENTS.md`](../AGENTS.md).
