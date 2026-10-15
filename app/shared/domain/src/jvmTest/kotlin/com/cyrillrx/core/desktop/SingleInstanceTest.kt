package com.cyrillrx.core.desktop

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class SingleInstanceTest {

    private val directory: File = createTempDirectory("instance").toFile()

    private val claimed = mutableListOf<SingleInstance>()

    @AfterTest
    fun tearDown() {
        claimed.forEach(SingleInstance::close)
        directory.deleteRecursively()
    }

    @Test
    fun `claims a directory nobody holds`() {
        assertNotNull(claim())
    }

    @Test
    fun `refuses a directory already held`() {
        claim()

        assertNull(claim())
    }

    @Test
    fun `lets the directory be claimed again once released`() {
        claim()?.close()

        assertNotNull(claim())
    }

    @Test
    fun `leaves a later claim alone when closed twice`() {
        val stale = checkNotNull(claim())
        stale.close()
        val holder = checkNotNull(claim())

        stale.close()

        assertComesForwardWhenAsked(holder)
    }

    @Test
    fun `brings the holder forward when asked`() {
        val holder = checkNotNull(claim())

        assertComesForwardWhenAsked(holder)
    }

    @Test
    fun `asks nothing of a directory nobody holds`() {
        SingleInstance.requestActivation(directory)
    }

    @Test
    fun `takes over a socket left behind by a crash`() {
        directory.resolve("instance.sock").writeText("left behind")
        val holder = checkNotNull(claim())

        assertComesForwardWhenAsked(holder)
    }

    @Test
    fun `claims a directory the system cannot lock`() {
        // A directory where the lock file belongs: opening it for writing fails on every system,
        // where a permission bit is ignored by some and by root on all of them.
        directory.resolve("instance.lock").mkdirs()

        assertNotNull(claim())

        assertFalse(directory.resolve("instance.sock").exists())
    }

    @Test
    fun `refuses a second claim from this process even without a lock`() {
        directory.resolve("instance.lock").mkdirs()
        claim()

        assertNull(claim())
    }

    private fun assertComesForwardWhenAsked(holder: SingleInstance) = runBlocking {
        withTimeout(5.seconds) {
            val request = async(start = CoroutineStart.UNDISPATCHED) { holder.activationRequests.first() }
            SingleInstance.requestActivation(directory)
            request.await()
        }
    }

    private fun claim(): SingleInstance? = SingleInstance.claim(directory)?.also(claimed::add)
}
