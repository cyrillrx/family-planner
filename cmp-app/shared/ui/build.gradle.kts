import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            // Kept as "Shared" so iosApp/ContentView.swift keeps importing `Shared`.
            baseName = "Shared"
            isStatic = true
            // Without it Kotlin/Native cannot infer one and warns on every link.
            binaryOption("bundleId", "com.cyrillrx.family.ui")
        }
    }

    // Shipped, ranked second (ADR-002), and what jvmTest and Kover run on.
    jvm()

    android {
        namespace = "com.cyrillrx.family.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.core)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

sonar {
    properties {
        // Absolute: the report is then found whatever base directory Sonar resolves against.
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/kover/reportJvm.xml").get().asFile.absolutePath,
        )
        // Sonar indexes these files either way and reads their absence from the report as zero
        // coverage. Every entry here has its counterpart in the kover block below: Kover matches
        // class names, Sonar matches file paths. See the coverage policy in AGENTS.md.
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/presentation/component/**",
                "**/presentation/theme/**",
                "**/navigation/**",
                "**/*Screen.kt",
                "**/app/**",
                "**/androidMain/**",
                "**/iosMain/**",
            ).joinToString(","),
        )
    }
}

kover {
    reports {
        filters {
            // Coverage only comes from jvmTest and no Compose UI test feeds Kover, so measuring
            // composables would only count tests that are never collected.
            excludes {
                classes(
                    "*.presentation.component.*",
                    "*.presentation.theme.*",
                    "*.navigation.*",
                    "*.ComposableSingletons*",
                    "*Screen",
                    "*ScreenKt",
                    "*.app.*",
                    // Generated: Compose resources accessors.
                    "*.generated.resources.*",
                )
            }
        }
    }
}

ktlint {
    debug.set(true)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    // Permissive on the UI module, strict on core — same split as kmp-ttrpg-companion.
    ignoreFailures.set(true)
}
