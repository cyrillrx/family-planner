import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

// Firebase options are not in the repository: it is public, and `.gitignore` already keeps
// `google-services.json` and `GoogleService-Info.plist` out. They come from `local.properties`,
// read through a provider so the configuration cache stays valid.
val firebaseProperties = providers.fileContents(
    rootProject.layout.projectDirectory.file("local.properties"),
).asText.map { text ->
    val properties = Properties().apply { load(text.reader()) }
    properties.stringPropertyNames()
        .filter { it.startsWith("firebase.") }
        .associateWith { properties.getProperty(it).trim() }
}.orElse(emptyMap())

val generateFirebaseConfig = tasks.register("generateFirebaseConfig") {
    val properties = firebaseProperties
    val outputDir = layout.buildDirectory.dir("generated/firebase/kotlin")
    inputs.property("firebaseProperties", properties)
    outputs.dir(outputDir)

    doLast {
        fun value(key: String) = properties.get()["firebase.$key"].orEmpty()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\\$")

        val file = outputDir.get().asFile.resolve("com/cyrillrx/family/firebase/FirebaseConfig.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.cyrillrx.family.firebase

            internal object FirebaseConfig {
                val projectId: String = "${value("projectId")}"
                val gcmSenderId: String = "${value("gcmSenderId")}"
                val storageBucket: String = "${value("storageBucket")}"
                val androidApplicationId: String = "${value("androidApplicationId")}"
                val androidApiKey: String = "${value("androidApiKey")}"
                val iosApplicationId: String = "${value("iosApplicationId")}"
                val iosApiKey: String = "${value("iosApiKey")}"
                val jvmApplicationId: String = "${value("jvmApplicationId")}"
                val jvmApiKey: String = "${value("jvmApiKey")}"
            }

            """.trimIndent(),
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Domain"
            isStatic = true
            // Without it Kotlin/Native cannot infer one and warns on every link.
            binaryOption("bundleId", "com.cyrillrx.family.domain")
        }
    }

    jvm()

    android {
        namespace = "com.cyrillrx.family.domain"
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
        commonMain {
            kotlin.srcDir(generateFirebaseConfig)
        }
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.kotlinx.coroutinesCore)
        }
        // Not in commonMain: on iOS the SDK does not link the Firebase frameworks transitively,
        // so that target needs CocoaPods in the build before it can carry the dependency.
        androidMain.dependencies {
            implementation(libs.gitlive.firebase.app)
        }
        jvmMain.dependencies {
            implementation(libs.gitlive.firebase.app)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
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
