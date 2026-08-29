# cmp-app

Kotlin Multiplatform client for Family Planner, targeting Android, iOS and Desktop with a shared Compose Multiplatform UI.

## Modules

| Module        | Contains                                                                                                      |
|---------------|---------------------------------------------------------------------------------------------------------------|
| `shared/core` | Domain and data layers. No Compose dependency, measured in full by Kover.                                     |
| `shared/ui`   | Compose UI. Ships to iOS as the `Shared` framework.                                                           |
| `androidApp`  | Android application wrapper.                                                                                  |
| `desktopApp`  | JVM application wrapper. Development target — it runs the tests and produces coverage, it is not distributed. |
| `iosApp`      | Xcode project. Its build phase calls `:shared:ui:embedAndSignAppleFrameworkForXcode`.                         |

The reasoning behind this split, and behind the absence of a Web target, is in [ADR-001](../docs/adr/adr-001-kmp-client-targets.md).

## Commands

```bash
./gradlew build                      # Build every target
./gradlew jvmTest                    # Run the JVM tests
./gradlew koverXmlReportJvm          # Coverage reports read by SonarCloud
./gradlew ktlintCheck                # Check formatting
./gradlew ktlintFormat               # Auto-fix formatting
./gradlew :desktopApp:run            # Run on Desktop
./gradlew :androidApp:installDebug   # Install on Android
```

iOS builds from `iosApp/iosApp.xcodeproj` in Xcode.

Contribution rules, including the coverage policy, are in [`AGENTS.md`](../AGENTS.md).
