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
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.sonarqube)
}

// Application wrappers hold no analyzable code, and scanning them trips a
// scanner/AGP 9 incompatibility (sonarResolver queries res providers before
// their producing task runs).
listOf(":androidApp", ":desktopApp").forEach { path ->
    project(path) {
        extensions.configure<SonarExtension>("sonar") {
            isSkipProject = true
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "cyrillrx_family-planner")
        property("sonar.organization", "cyrillrx")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
