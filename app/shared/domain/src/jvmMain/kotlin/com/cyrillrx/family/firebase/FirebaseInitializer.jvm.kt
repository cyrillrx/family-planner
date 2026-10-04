package com.cyrillrx.family.firebase

import android.app.Application
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.Properties

private var platformPrepared = false

actual fun initializeFirebase(context: Any?) {
    val options = jvmOptions() ?: return
    val directory = applicationDataDirectory() ?: return

    if (!platformPrepared) {
        FirebasePlatform.initializeFirebasePlatform(DesktopFirebasePlatform(directory))
        platformPrepared = true
    }

    val application = Application()
    if (Firebase.apps(application).isEmpty()) Firebase.initialize(application, options)
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
 *
 * TODO(#50): cover the persistence and the data directory choice.
 */
private class DesktopFirebasePlatform(private val directory: File) : FirebasePlatform() {

    private val file = directory.resolve("firebase.properties")

    private val stored = readStore()

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

    override fun getDatabasePath(name: String): File = directory.resolve(name)

    private fun readStore(): Properties = try {
        Properties().apply { if (file.exists()) file.inputStream().use { load(it) } }
    } catch (unreadable: IllegalArgumentException) {
        Properties()
    } catch (unreadable: IOException) {
        Properties()
    }

    @Synchronized
    private fun flush() {
        val pending = directory.resolve("firebase.properties.tmp")
        pending.outputStream().use { stored.store(it, null) }
        Files.move(pending.toPath(), file.toPath(), REPLACE_EXISTING, ATOMIC_MOVE)
    }
}

private fun applicationDataDirectory(): File? {
    val home = File(System.getProperty("user.home"))
    val os = System.getProperty("os.name").lowercase()

    val base = when {
        os.contains("mac") -> home.resolve("Library/Application Support")
        os.contains("win") -> environmentDirectory("APPDATA") ?: home
        else -> environmentDirectory("XDG_DATA_HOME") ?: home.resolve(".local/share")
    }

    return base.resolve("FamilyPlanner").apply { mkdirs() }.takeIf { it.isDirectory }
}

private fun environmentDirectory(name: String): File? = System.getenv(name)?.takeIf { it.isNotBlank() }?.let(::File)
