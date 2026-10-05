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
 * TODO(#51): keep a second instance from overwriting this store.
 */
internal class DesktopFirebasePlatform(private val directory: File) : FirebasePlatform() {

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
        try {
            replaceStoreFile()
        } catch (unwritable: IOException) {
            Unit
        }
    }

    private fun replaceStoreFile() {
        val pending = Files.createTempFile(directory.toPath(), "firebase", ".properties")
        try {
            Files.newOutputStream(pending).use { stored.store(it, null) }
            Files.move(pending, file.toPath(), REPLACE_EXISTING, ATOMIC_MOVE)
        } finally {
            Files.deleteIfExists(pending)
        }
    }
}

// The lookups are parameters so the choice can be exercised for an operating system other
// than the one running the test.
internal fun applicationDataDirectory(
    home: File = File(System.getProperty("user.home")),
    operatingSystem: String = System.getProperty("os.name"),
    environment: (String) -> String? = System::getenv,
): File? {
    val os = operatingSystem.lowercase()

    val base = when {
        os.contains("mac") -> home.resolve("Library/Application Support")
        os.contains("win") -> environmentDirectory(environment, "APPDATA") ?: home
        else -> environmentDirectory(environment, "XDG_DATA_HOME") ?: home.resolve(".local/share")
    }

    return base.resolve("FamilyPlanner").apply { mkdirs() }.takeIf { it.isDirectory }
}

private fun environmentDirectory(environment: (String) -> String?, name: String): File? =
    environment(name)?.takeIf { it.isNotBlank() }?.let(::File)
