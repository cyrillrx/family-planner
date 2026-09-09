package com.cyrillrx.family.group.data

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.RegisterUserError
import com.cyrillrx.family.group.domain.User
import com.cyrillrx.family.group.domain.UserId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ApiUserRepositoryTest {

    @Test
    fun `registers a user through the api`() = runTest {
        val repository = ApiUserRepository(RamUserApi())

        val result = repository.register(cyril())

        assertEquals(Result.Success(cyril()), result)
    }

    @Test
    fun `turns a transport failure into a domain error`() = runTest {
        val repository = ApiUserRepository(ThrowingUserApi(IllegalStateException("no network")))

        assertEquals(
            Result.Failure(RegisterUserError.Unknown),
            repository.register(cyril()),
        )
    }

    @Test
    fun `lets a cancellation through instead of reporting it as a failure`() = runTest {
        val repository = ApiUserRepository(ThrowingUserApi(CancellationException("cancelled")))

        assertFailsWith<CancellationException> { repository.register(cyril()) }
    }

    @Test
    fun `observes a registered user`() = runTest {
        val api = RamUserApi()
        val repository = ApiUserRepository(api)
        repository.register(cyril())

        assertEquals(cyril(), repository.observeUser(UserId("user-1")).first())
    }

    @Test
    fun `observes null for a user it does not know`() = runTest {
        val repository = ApiUserRepository(RamUserApi())

        assertNull(repository.observeUser(UserId("nobody")).first())
    }

    @Test
    fun `observes only the users asked for`() = runTest {
        val repository = ApiUserRepository(
            RamUserApi(listOf(apiUser("user-1"), apiUser("user-2"), apiUser("user-3"))),
        )

        assertEquals(
            listOf(user("user-1"), user("user-3")),
            repository.observeUsers(setOf(UserId("user-1"), UserId("user-3"))).first(),
        )
    }

    @Test
    fun `observes nothing when nothing is asked for`() = runTest {
        val repository = ApiUserRepository(RamUserApi(listOf(apiUser("user-1"))))

        assertEquals(emptyList(), repository.observeUsers(emptySet()).first())
    }

    private fun cyril() = User(id = UserId("user-1"), displayName = "Cyril")

    private fun user(id: String) = User(id = UserId(id), displayName = id)

    private fun apiUser(id: String) = ApiUser(id = id, displayName = id)

    private class ThrowingUserApi(private val failure: Throwable) : UserApi {
        override suspend fun register(request: ApiRegisterUserRequest): ApiUser = throw failure

        override fun observeUsers(ids: Set<String>): Flow<List<ApiUser>> = flowOf(emptyList())
    }
}
