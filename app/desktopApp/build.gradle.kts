import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.app.shared.ui)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.cyrillrx.family.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.cyrillrx.family"
            packageVersion = "1.0.0"
            // The Firebase SDK needs these, and the packaged runtime only bundles what is listed.
            modules("java.compiler", "java.naming", "java.sql")
        }

        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}
