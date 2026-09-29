import org.sonarqube.gradle.SonarExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.sonarqube)
}

// Application wrappers hold no analyzable code, and scanning them trips a
// scanner/AGP 9 incompatibility (sonarResolver queries res providers before
// their producing task runs).
listOf(":app:androidApp", ":app:desktopApp").forEach { path ->
    project(path) {
        extensions.configure<SonarExtension>("sonar") {
            isSkipProject = true
        }
    }
}

kover {
    reports {
        filters {
            // Coverage only comes from jvmTest and no Compose UI test feeds Kover, so measuring
            // composables would only count tests that are never collected.
            // TODO(#28): `*.navigation.*` also drops ordinary logic. Narrow it.
            excludes {
                classes(
                    "*.presentation.component.*",
                    "*.presentation.theme.*",
                    "*.navigation.*",
                    "*.ComposableSingletons*",
                    "*Screen",
                    "*ScreenKt",
                    // Nested: the lambdas of a screen compile into ScreenKt$Screen$1$1 and
                    // the exact-match pattern above does not reach them.
                    "*ScreenKt$*",
                    // The application's root composable, and the back stack it remembers.
                    "*.app.AppKt",
                    "*.app.AppKt$*",
                    // The entry point: no test starts an engine.
                    "*.server.ApplicationKt*",
                    // Generated: Compose resources accessors.
                    "*.generated.resources.*",
                )
            }
        }
    }
}

dependencies {
    kover(projects.core.model)
    kover(projects.server)
    kover(projects.app.shared.domain)
    kover(projects.app.shared.ui)
}

sonar {
    properties {
        property("sonar.projectKey", "cyrillrx_family-planner")
        property("sonar.organization", "cyrillrx")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
