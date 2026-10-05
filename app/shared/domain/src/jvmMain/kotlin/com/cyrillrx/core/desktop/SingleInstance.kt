package com.cyrillrx.core.desktop

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.IOException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
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

class SingleInstance private constructor(
    private val lock: FileLock?,
    private val socket: Path,
) : AutoCloseable {

    private val requests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val activationRequests: Flow<Unit> = requests.asSharedFlow()

    private val server: ServerSocketChannel? = listen()

    override fun close() {
        server?.close()
        try {
            Files.deleteIfExists(socket)
        } finally {
            lock?.channel()?.close()
        }
    }

    // Without the socket, the lock still keeps a second instance out.
    private fun listen(): ServerSocketChannel? {
        val server = openSocket() ?: return null

        return try {
            server.bind(UnixDomainSocketAddress.of(socket))
            thread(isDaemon = true, name = "single-instance") { accept(server) }
            server
        } catch (unavailable: IOException) {
            server.close()
            null
        }
    }

    private fun openSocket(): ServerSocketChannel? = try {
        Files.deleteIfExists(socket)
        ServerSocketChannel.open(StandardProtocolFamily.UNIX)
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
        } catch (stopped: IOException) {
            Unit
        }
    }

    companion object {

        // A directory the system cannot lock is claimed without one: refusing it would leave the
        // application unable to start at all.
        fun claim(directory: File): SingleInstance? {
            val socket = directory.resolve(SOCKET_FILE).toPath()
            val channel = openLockFile(directory.resolve(LOCK_FILE)) ?: return SingleInstance(null, socket)

            val lock = try {
                channel.tryLock()
            } catch (heldInThisProcess: OverlappingFileLockException) {
                null
            } catch (unlockable: IOException) {
                channel.close()
                return SingleInstance(null, socket)
            }
            if (lock == null) channel.close()

            return lock?.let { held -> SingleInstance(held, socket) }
        }

        fun requestActivation(directory: File) {
            try {
                SocketChannel.open(UnixDomainSocketAddress.of(directory.resolve(SOCKET_FILE).toPath())).close()
            } catch (unanswered: IOException) {
                Unit
            } catch (unsupported: UnsupportedOperationException) {
                Unit
            }
        }

        private fun openLockFile(file: File): FileChannel? = try {
            FileChannel.open(file.toPath(), CREATE, WRITE)
        } catch (unwritable: IOException) {
            null
        }

        private const val LOCK_FILE = "instance.lock"
        private const val SOCKET_FILE = "instance.sock"
    }
}
