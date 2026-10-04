package com.cyrillrx.family.firebase

import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import java.io.File
import java.util.Properties

private var platformPrepared = false

actual fun initializeFirebase(context: Any?) {
    val options = jvmOptions() ?: return

    if (!platformPrepared) {
        FirebasePlatform.initializeFirebasePlatform(DesktopFirebasePlatform(applicationDataDirectory()))
        platformPrepared = true
    }

    if (Firebase.apps(context).isEmpty()) Firebase.initialize(context, options)
}

private fun jvmOptions(): FirebaseOptions? {
    val applicationId = FirebaseConfig.jvmApplicationId
    val apiKey = FirebaseConfig.jvmApiKey
    if (applicationId.isEmpty() || apiKey.isEmpty() || FirebaseConfig.projectId.isEmpty()) return null

    return FirebaseOptions(
        applicationId = applicationId,
        apiKey = apiKey,
        projectId = FirebaseConfig.projectId,
        gcmSenderId = FirebaseConfig.gcmSenderId.ifEmpty { null },
        storageBucket = FirebaseConfig.storageBucket.ifEmpty { null },
    )
}

/**
 * The sample in the SDK's documentation keeps its store in a map and leaves the database in the
 * system temporary directory, which would cost the offline cache on every restart.
 */
private class DesktopFirebasePlatform(private val directory: File) : FirebasePlatform() {

    private val file = directory.resolve("firebase.properties")

    private val stored = Properties().apply {
        if (file.exists()) file.inputStream().use { load(it) }
    }

    override fun store(key: String, value: String) {
        stored.setProperty(key, value)
        flush()
    }

    override fun retrieve(key: String): String? = stored.getProperty(key)

    override fun clear(key: String) {
        stored.remove(key)
        flush()
    }

    override fun log(msg: String) = Unit

    override fun getDatabasePath(name: String): File = directory.resolve(name).apply { mkdirs() }

    private fun flush() = file.outputStream().use { stored.store(it, null) }
}

private fun applicationDataDirectory(): File {
    val home = File(System.getProperty("user.home"))
    val os = System.getProperty("os.name").lowercase()

    val base = when {
        os.contains("mac") -> home.resolve("Library/Application Support")
        os.contains("win") -> System.getenv("APPDATA")?.let(::File) ?: home
        else -> System.getenv("XDG_DATA_HOME")?.let(::File) ?: home.resolve(".local/share")
    }

    return base.resolve("FamilyPlanner").apply { mkdirs() }
}
