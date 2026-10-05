package com.cyrillrx.core.desktop

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.IOException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import kotlin.concurrent.thread

/**
 * Ownership of an application data directory by a single running process.
 *
 * The lock is held by the operating system, which releases it when the process dies, so a crash
 * never leaves the directory claimed. A later process asks the owner to come forward through
 * [requestActivation] instead of opening the directory a second time.
 */
class SingleInstance private constructor(
    private val lock: FileLock,
    private val socket: Path,
) : AutoCloseable {

    private val requests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Emits each time another process asks this one to come forward. */
    val activationRequests: Flow<Unit> = requests.asSharedFlow()

    private val server: ServerSocketChannel? = listen()

    override fun close() {
        server?.close()
        Files.deleteIfExists(socket)
        lock.channel().close()
    }

    // The lock alone keeps the directory safe: without the socket, a second instance still
    // exits, it just cannot bring this one forward.
    private fun listen(): ServerSocketChannel? = try {
        Files.deleteIfExists(socket)
        ServerSocketChannel.open(StandardProtocolFamily.UNIX)
            .bind(UnixDomainSocketAddress.of(socket))
            .also { server -> thread(isDaemon = true, name = "single-instance") { accept(server) } }
    } catch (unavailable: IOException) {
        null
    } catch (unsupported: UnsupportedOperationException) {
        null
    }

    private fun accept(server: ServerSocketChannel) {
        try {
            while (true) {
                server.accept().close()
                requests.tryEmit(Unit)
            }
        } catch (closed: ClosedChannelException) {
            Unit
        } catch (failed: IOException) {
            Unit
        }
    }

    companion object {

        /** Claims [directory] for this process, or gives nothing back when another one holds it. */
        fun claim(directory: File): SingleInstance? {
            val lock = tryLock(directory.resolve(LOCK_FILE)) ?: return null

            return SingleInstance(lock, directory.resolve(SOCKET_FILE).toPath())
        }

        /** Asks the process holding [directory] to come forward. Does nothing when none answers. */
        fun requestActivation(directory: File) {
            try {
                SocketChannel.open(UnixDomainSocketAddress.of(directory.resolve(SOCKET_FILE).toPath())).close()
            } catch (unanswered: IOException) {
                Unit
            }
        }

        private fun tryLock(file: File): FileLock? {
            val channel = FileChannel.open(file.toPath(), CREATE, WRITE)
            val lock = try {
                channel.tryLock()
            } catch (heldInThisProcess: OverlappingFileLockException) {
                null
            }
            if (lock == null) channel.close()

            return lock
        }

        private const val LOCK_FILE = "instance.lock"
        private const val SOCKET_FILE = "instance.sock"
    }
}
