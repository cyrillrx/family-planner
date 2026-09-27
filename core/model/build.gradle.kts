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
        // Absolute: the report is then found whatever base directory Sonar resolves against.
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/kover/reportJvm.xml").get().asFile.absolutePath,
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
