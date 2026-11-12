package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.UserStore
import com.cyrillrx.family.group.document.UserDocument
import com.cyrillrx.family.group.model.User
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StoreUserRepositoryTest {

    @Test
    fun `has no registered user on a device that never signed in`() = runTest {
        assertEquals(
            Result.Success(Registration.NotRegistered),
            StoreUserRepository(FakeUserStore()).registeredUserId(),
        )
    }

    @Test
    fun `has no registered user when the credential addresses nothing`() = runTest {
        val store = FakeUserStore(credential = "uid-1")

        assertEquals(Result.Success(Registration.NotRegistered), StoreUserRepository(store).registeredUserId())
    }

    @Test
    fun `finds the user the credential addresses`() = runTest {
        val store = FakeUserStore(credential = "uid-1")
        store.documents["uid-1"] = UserDocument(id = "user-1", displayName = "Cyril")

        assertEquals(
            Result.Success(Registration.Registered(UserId("user-1"))),
            StoreUserRepository(store).registeredUserId(),
        )
    }

    @Test
    fun `reads a stored user without an identifier as no user`() = runTest {
        val store = FakeUserStore(credential = "uid-1")
        store.documents["uid-1"] = UserDocument(displayName = "Cyril")

        assertEquals(Result.Success(Registration.NotRegistered), StoreUserRepository(store).registeredUserId())
    }

    @Test
    fun `cannot tell who is registered when the store cannot be read`() = runTest {
        val store = FakeUserStore(credential = "uid-1", readFailure = UNREACHABLE)

        assertEquals(Result.Failure(UNREACHABLE), StoreUserRepository(store).registeredUserId())
    }

    @Test
    fun `reports a defect of the read as unexpected rather than as an unreachable store`() = runTest {
        val defect = IllegalStateException("defect")
        val store = FakeUserStore(credential = "uid-1", thrown = defect)

        assertEquals(Result.Failure(Unexpected(defect)), StoreUserRepository(store).registeredUserId())
    }

    @Test
    fun `lets a cancellation of the read through`() = runTest {
        val store = FakeUserStore(credential = "uid-1", thrown = CancellationException("left the screen"))

        assertFailsWith<CancellationException> { StoreUserRepository(store).registeredUserId() }
    }

    @Test
    fun `signs in before registering`() = runTest {
        val store = FakeUserStore()

        StoreUserRepository(store).register(cyril())

        assertEquals("anonymous-uid", store.credential())
    }

    @Test
    fun `keeps the credential the device already holds`() = runTest {
        val store = FakeUserStore(credential = "uid-1")

        StoreUserRepository(store).register(cyril())

        assertEquals(0, store.anonymousSignIns)
    }

    @Test
    fun `does not read the store after a fresh sign-in`() = runTest {
        val store = FakeUserStore()

        StoreUserRepository(store).register(cyril())

        assertEquals(0, store.reads)
    }

    @Test
    fun `stores the user under the credential that authenticates it`() = runTest {
        val store = FakeUserStore(credential = "uid-1")

        StoreUserRepository(store).register(cyril())

        assertEquals(
            UserDocument(id = "user-1", displayName = "Cyril", authenticatedId = "uid-1"),
            store.documents["uid-1"],
        )
    }

    @Test
    fun `answers with the user and its credential`() = runTest {
        val store = FakeUserStore(credential = "uid-1")

        assertEquals(
            Result.Success(cyril().copy(authenticatedId = "uid-1")),
            StoreUserRepository(store).register(cyril()),
        )
    }

    @Test
    fun `registering again renames the user and keeps its identifier`() = runTest {
        val store = FakeUserStore(credential = "uid-1")
        store.documents["uid-1"] = UserDocument(id = "user-1", displayName = "Cyril", authenticatedId = "uid-1")

        StoreUserRepository(store).register(User(UserId("user-2"), "Cyrille"))

        assertEquals(
            UserDocument(id = "user-1", displayName = "Cyrille", authenticatedId = "uid-1"),
            store.documents["uid-1"],
        )
    }

    @Test
    fun `reports a store that refuses the registration`() = runTest {
        val store = FakeUserStore(credential = "uid-1", writeFailure = UNREACHABLE)

        assertEquals(
            Result.Failure(UNREACHABLE),
            StoreUserRepository(store).register(cyril()),
        )
    }

    @Test
    fun `lets a cancellation through rather than reporting it`() = runTest {
        val store = FakeUserStore(thrown = CancellationException("left the screen"))

        assertFailsWith<CancellationException> { StoreUserRepository(store).register(cyril()) }
    }

    @Test
    fun `reports a sign-in the store refused`() = runTest {
        val store = FakeUserStore(signInFailure = UNREACHABLE)

        assertEquals(Result.Failure(UNREACHABLE), StoreUserRepository(store).register(cyril()))
    }

    @Test
    fun `reports a defect of the registration as unexpected`() = runTest {
        val defect = IllegalStateException("defect")

        assertEquals(
            Result.Failure(Unexpected(defect)),
            StoreUserRepository(FakeUserStore(thrown = defect)).register(cyril()),
        )
    }

    @Test
    fun `writes nothing when it cannot tell which user the credential already addresses`() = runTest {
        val store = FakeUserStore(credential = "uid-1", readFailure = UNREACHABLE)

        StoreUserRepository(store).register(cyril())

        assertEquals(emptyMap(), store.documents)
    }

    private fun cyril() = User(UserId("user-1"), "Cyril")
}

private class FakeUserStore(
    private var credential: String? = null,
    private val signInFailure: StoreUnavailable? = null,
    private val readFailure: StoreUnavailable? = null,
    private val writeFailure: StoreUnavailable? = null,
    private val thrown: Exception? = null,
) : UserStore {

    val documents = mutableMapOf<String, UserDocument>()
    var anonymousSignIns = 0
        private set
    var reads = 0
        private set

    override suspend fun credential(): String? = credential

    override suspend fun signInAnonymously(): Result<String, StoreUnavailable> {
        thrown?.let { throw it }

        signInFailure?.let { return Result.Failure(it) }

        anonymousSignIns++

        return Result.Success("anonymous-uid".also { credential = it })
    }

    override suspend fun find(credential: String): Result<UserDocument?, StoreUnavailable> {
        thrown?.let { throw it }

        readFailure?.let { return Result.Failure(it) }

        reads++

        return Result.Success(documents[credential])
    }

    override suspend fun save(credential: String, document: UserDocument): Result<Unit, StoreUnavailable> {
        writeFailure?.let { return Result.Failure(it) }

        documents[credential] = document

        return Result.Success(Unit)
    }
}
