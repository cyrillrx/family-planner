package com.cyrillrx.family.firebase

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApplicationDataDirectoryTest {

    private val home: File = createTempDirectory("firebase-home").toFile()

    @AfterTest
    fun tearDown() {
        home.deleteRecursively()
    }

    @Test
    fun `follows the application support convention on macos`() {
        val directory = applicationDataDirectory(home, "Mac OS X") { null }

        assertEquals(home.resolve("Library/Application Support/FamilyPlanner"), directory)
    }

    @Test
    fun `follows the roaming profile on windows`() {
        val appData = home.resolve("AppData/Roaming")

        val directory = applicationDataDirectory(home, "Windows 11") { appData.toString() }

        assertEquals(appData.resolve("FamilyPlanner"), directory)
    }

    @Test
    fun `falls back to the home directory when windows names no roaming profile`() {
        val directory = applicationDataDirectory(home, "Windows 11") { null }

        assertEquals(home.resolve("FamilyPlanner"), directory)
    }

    @Test
    fun `follows the xdg data home elsewhere`() {
        val share = home.resolve("custom-share")

        val directory = applicationDataDirectory(home, "Linux") { share.toString() }

        assertEquals(share.resolve("FamilyPlanner"), directory)
    }

    @Test
    fun `falls back to the xdg default when the variable is blank`() {
        val directory = applicationDataDirectory(home, "Linux") { "   " }

        assertEquals(home.resolve(".local/share/FamilyPlanner"), directory)
    }

    @Test
    fun `creates the directory it names`() {
        val directory = applicationDataDirectory(home, "Linux") { null }

        assertTrue(directory?.isDirectory == true)
    }

    @Test
    fun `gives nothing back when the directory cannot be created`() {
        val blocked = home.resolve("blocked").apply { writeText("a file where a directory is needed") }

        assertNull(applicationDataDirectory(home, "Linux") { blocked.toString() })
    }
}
