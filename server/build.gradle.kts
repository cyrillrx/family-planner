plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
}

group = "com.cyrillrx.family"
version = "0.1.0"

application {
    mainClass = "com.cyrillrx.family.server.ApplicationKt"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(projects.core.model)

    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.logback)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
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
        // Paired with the kover exclusion at the root: Kover matches class names, Sonar file
        // paths, and an exclusion declared on one side only reads as zero coverage on the other.
        property("sonar.coverage.exclusions", "**/Application.kt")
    }
}

ktlint {
    debug.set(true)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
}
