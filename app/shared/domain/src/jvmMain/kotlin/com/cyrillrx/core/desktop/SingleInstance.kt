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
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class SingleInstance private constructor(
    private val directory: Path,
    private val lock: FileLock?,
    private val socket: Path?,
) : AutoCloseable {

    private val requests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val activationRequests: Flow<Unit> = requests.asSharedFlow()

    private val server: ServerSocketChannel? = socket?.let { listen(it) }

    private var released = false

    @Synchronized
    override fun close() {
        // The socket path outlives the lock: a second release would unlink the socket of whichever
        // instance claimed the directory in between.
        if (released) return
        released = true

        release { server?.close() }
        release { socket?.let(Files::deleteIfExists) }
        lock?.channel()?.let { channel ->
            release { channel.close() }
            lockChannels.remove(channel)
        }
        claimedDirectories.remove(directory)
    }

    // Without the socket, the lock still keeps a second instance out.
    private fun listen(socket: Path): ServerSocketChannel? {
        val server = openSocket(socket) ?: return null

        return try {
            server.bind(UnixDomainSocketAddress.of(socket))
            thread(isDaemon = true, name = "single-instance") { accept(server) }
            server
        } catch (unavailable: IOException) {
            server.close()
            null
        }
    }

    private fun openSocket(socket: Path): ServerSocketChannel? = try {
        Files.deleteIfExists(socket)
        ServerSocketChannel.open(StandardProtocolFamily.UNIX)
    } catch (unavailable: IOException) {
        null
    } catch (unsupported: UnsupportedOperationException) {
        null
    }

    private fun accept(server: ServerSocketChannel) {
        while (server.isOpen) {
            try {
                server.accept().use { requests.tryEmit(Unit) }
            } catch (stopped: ClosedChannelException) {
                return
            } catch (refused: IOException) {
                // A connection the peer drops between accept() and its return fails without
                // blocking, so a lasting failure would spin the thread rather than wait.
                if (!waitBeforeRetry()) return
            }
        }
    }

    private fun waitBeforeRetry(): Boolean = try {
        Thread.sleep(RETRY_DELAY_MS)
        true
    } catch (interrupted: InterruptedException) {
        false
    }

    private inline fun release(step: () -> Unit) = try {
        step()
    } catch (unreleasable: IOException) {
        Unit
    }

    companion object {

        private val claimedDirectories: MutableSet<Path> = ConcurrentHashMap.newKeySet()

        // A lock channel is never left to the collector: once collected it gets closed, and closing
        // any descriptor of the file releases every POSIX lock the process holds on it.
        private val lockChannels: MutableSet<FileChannel> = ConcurrentHashMap.newKeySet()

        // A second claim from this process never opens the lock file, whose every descriptor shares
        // the lock the holder took.
        fun claim(directory: File): SingleInstance? {
            val key = claimKey(directory)
            if (!claimedDirectories.add(key)) return null

            var instance: SingleInstance? = null
            try {
                instance = lockDirectory(directory, key)
            } finally {
                if (instance == null) claimedDirectories.remove(key)
            }
            return instance
        }

        // Two spellings of one directory, through a symlink or a different case, must give one key.
        private fun claimKey(directory: File): Path = try {
            directory.toPath().toRealPath()
        } catch (unresolvable: IOException) {
            directory.absoluteFile.normalize().toPath()
        }

        // A directory the system cannot lock is claimed without one: refusing it would leave the
        // application unable to start at all. That instance holds no socket either, as deleting
        // and rebinding it would cut off the instance that does hold the lock.
        private fun lockDirectory(directory: File, key: Path): SingleInstance? {
            val channel = openLockFile(directory.resolve(LOCK_FILE)) ?: return SingleInstance(key, null, null)

            val lock = try {
                channel.tryLock()
            } catch (heldInThisProcess: OverlappingFileLockException) {
                // Closing this channel would release the lock held through the other one.
                lockChannels.add(channel)
                return null
            } catch (unlockable: IOException) {
                channel.close()
                return SingleInstance(key, null, null)
            }
            if (lock == null) {
                channel.close()
                return null
            }

            lockChannels.add(channel)
            return SingleInstance(key, lock, socketPath(directory))
        }

        fun requestActivation(directory: File) {
            try {
                SocketChannel.open(UnixDomainSocketAddress.of(socketPath(directory))).close()
            } catch (unanswered: IOException) {
                Unit
            } catch (unsupported: UnsupportedOperationException) {
                Unit
            }
        }

        // Both ends of the hand-off derive the path here: they are useless unless they agree.
        private fun socketPath(directory: File): Path = directory.resolve(SOCKET_FILE).toPath()

        private fun openLockFile(file: File): FileChannel? = try {
            FileChannel.open(file.toPath(), CREATE, WRITE)
        } catch (unwritable: IOException) {
            null
        }

        private const val LOCK_FILE = "instance.lock"
        private const val SOCKET_FILE = "instance.sock"
        private const val RETRY_DELAY_MS = 100L
    }
}
