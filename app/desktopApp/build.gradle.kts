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
            modules("java.compiler", "java.instrument", "java.naming", "java.sql", "jdk.unsupported")
        }

        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}

// SPIKE — not for commit
dependencies {
    implementation(libs.gitlive.firebase.auth)
    implementation(libs.gitlive.firebase.app)
    implementation(libs.gitlive.firebase.firestore)
}
tasks.register<JavaExec>("runLinkSpike") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.cyrillrx.family.spike.LinkSpikeKt")
    workingDir = rootProject.projectDir
    standardInput = System.`in`
}
tasks.register<JavaExec>("runSignInSpike") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.cyrillrx.family.spike.SignInSpikeKt")
    workingDir = rootProject.projectDir
}
