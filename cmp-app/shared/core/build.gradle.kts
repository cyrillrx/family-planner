import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Core"
            isStatic = true
            // Without it Kotlin/Native cannot infer one and warns on every link.
            binaryOption("bundleId", "com.cyrillrx.family.core")
        }
    }

    // Not a shipped target: it is what jvmTest and Kover run on.
    jvm()

    android {
        namespace = "com.cyrillrx.family.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
        withHostTest {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

sonar {
    properties {
        // Absolute: the report is then found whatever base directory Sonar resolves against.
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/kover/reportJvm.xml").get().asFile.absolutePath,
        )
        // Sonar indexes these files either way and reads their absence from the report as zero
        // coverage. See the coverage policy in AGENTS.md.
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/androidMain/**",
                "**/iosMain/**",
            ).joinToString(","),
        )
    }
}

ktlint {
    debug.set(true)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
}
