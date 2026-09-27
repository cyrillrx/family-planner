import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    // One compilation per artifact format, not per consumer: Android reads the jvm variant,
    // Kotlin/Native cannot. No android block — nothing here touches the platform (ADR-005).
    iosArm64()
    iosSimulatorArm64()

    jvm {
        compilerOptions {
            // Gradle carries no jvm.version on these variants, so a mismatch with the Android
            // app would surface at dexing rather than at resolution.
            jvmTarget = JvmTarget.JVM_21
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
        // One aggregated report for the whole build: the tests that exercise a type no longer
        // live in its module. Absolute, so it is found whatever base directory Sonar resolves
        // against; each module reads the same file and finds only its own sources in it.
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            rootProject.layout.buildDirectory.file("reports/kover/report.xml").get().asFile.absolutePath,
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
