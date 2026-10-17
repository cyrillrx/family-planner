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
import kotlin.test.assertNull

class StoreUserRepositoryTest {

    @Test
    fun `has no registered user on a device that never signed in`() = runTest {
        assertNull(StoreUserRepository(FakeUserStore()).registeredUserId())
    }

    @Test
    fun `has no registered user when the credential addresses nothing`() = runTest {
        val store = FakeUserStore(credential = "uid-1")

        assertNull(StoreUserRepository(store).registeredUserId())
    }

    @Test
    fun `finds the user the credential addresses`() = runTest {
        val store = FakeUserStore(credential = "uid-1")
        store.documents["uid-1"] = UserDocument(id = "user-1", displayName = "Cyril")

        assertEquals(UserId("user-1"), StoreUserRepository(store).registeredUserId())
    }

    @Test
    fun `reads a stored user without an identifier as no user`() = runTest {
        val store = FakeUserStore(credential = "uid-1")
        store.documents["uid-1"] = UserDocument(displayName = "Cyril")

        assertNull(StoreUserRepository(store).registeredUserId())
    }

    @Test
    fun `has no registered user when the store cannot be read`() = runTest {
        val store = FakeUserStore(credential = "uid-1", failure = IllegalStateException("unreachable"))

        assertNull(StoreUserRepository(store).registeredUserId())
    }

    @Test
    fun `signs in before registering`() = runTest {
        val store = FakeUserStore()

        StoreUserRepository(store).register(cyril())

        assertEquals("anonymous-uid", store.credential())
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
        val store = FakeUserStore(failure = IllegalStateException("permission denied"))

        assertEquals(
            Result.Failure(RegisterUserError.Unknown),
            StoreUserRepository(store).register(cyril()),
        )
    }

    @Test
    fun `lets a cancellation through rather than reporting it`() = runTest {
        val store = FakeUserStore(failure = CancellationException("left the screen"))

        assertFailsWith<CancellationException> { StoreUserRepository(store).register(cyril()) }
    }

    private fun cyril() = User(UserId("user-1"), "Cyril")
}

private class FakeUserStore(
    private var credential: String? = null,
    private val failure: Exception? = null,
) : UserStore {

    val documents = mutableMapOf<String, UserDocument>()

    override suspend fun credential(): String? = credential

    override suspend fun signIn(): String {
        failure?.let { throw it }

        return credential ?: "anonymous-uid".also { credential = it }
    }

    override suspend fun find(credential: String): UserDocument? {
        failure?.let { throw it }

        return documents[credential]
    }

    override suspend fun save(credential: String, document: UserDocument) {
        failure?.let { throw it }

        documents[credential] = document
    }
}
