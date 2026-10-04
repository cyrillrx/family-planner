package com.cyrillrx.family.firebase

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopFirebasePlatformTest {

    private val directory: File = createTempDirectory("firebase-platform").toFile()

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `gives back what it stored`() {
        val platform = DesktopFirebasePlatform(directory)

        platform.store("token", "abc")

        assertEquals("abc", platform.retrieve("token"))
    }

    @Test
    fun `knows nothing of a key it never stored`() {
        assertNull(DesktopFirebasePlatform(directory).retrieve("absent"))
    }

    @Test
    fun `forgets a cleared key`() {
        val platform = DesktopFirebasePlatform(directory)
        platform.store("token", "abc")

        platform.clear("token")

        assertNull(platform.retrieve("token"))
    }

    @Test
    fun `keeps the other keys when one is cleared`() {
        val platform = DesktopFirebasePlatform(directory)
        platform.store("token", "abc")
        platform.store("refresh", "def")

        platform.clear("token")

        assertEquals("def", platform.retrieve("refresh"))
    }

    @Test
    fun `hands what one instance stored to the next one`() {
        DesktopFirebasePlatform(directory).store("token", "abc")

        assertEquals("abc", DesktopFirebasePlatform(directory).retrieve("token"))
    }

    @Test
    fun `starts empty rather than throwing on a store it cannot read`() {
        directory.resolve("firebase.properties").writeText("broken=\\uZZZZ")

        assertNull(DesktopFirebasePlatform(directory).retrieve("broken"))
    }

    @Test
    fun `leaves no temporary file behind`() {
        DesktopFirebasePlatform(directory).store("token", "abc")

        assertEquals(listOf("firebase.properties"), directory.list()?.sorted())
    }

    @Test
    fun `puts the database beside the store rather than in the system temp`() {
        val path = DesktopFirebasePlatform(directory).getDatabasePath("firestore")

        assertEquals(directory.resolve("firestore"), path)
        assertTrue(path.toString().startsWith(directory.toString()))
    }
}
