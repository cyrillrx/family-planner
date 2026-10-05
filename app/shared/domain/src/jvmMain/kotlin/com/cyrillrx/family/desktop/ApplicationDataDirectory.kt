package com.cyrillrx.family.desktop

import java.io.File

// The lookups are parameters so the choice can be exercised for an operating system other
// than the one running the test.
fun applicationDataDirectory(
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
