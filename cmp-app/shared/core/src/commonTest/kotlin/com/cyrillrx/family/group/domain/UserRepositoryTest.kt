package com.cyrillrx.family.group.domain

import com.cyrillrx.core.data.model.ApiError
import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.UserApi
import com.cyrillrx.family.group.data.model.ApiRegisterUserRequest
import com.cyrillrx.family.group.data.model.ApiUser
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UserRepositoryTest {

    @Test
    fun `hands the user to the api as a registration request`() = runTest {
        val api = RecordingUserApi(ApiResponse(payload = cyril()))

        UserRepositoryImpl(api).register(User(UserId("user-1"), "Cyril"))

        assertEquals(ApiRegisterUserRequest(id = "user-1", displayName = "Cyril"), api.lastRequest)
    }

    @Test
    fun `turns a registered answer into the domain`() = runTest {
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse(payload = cyril())))

        assertEquals(
            Result.Success(User(UserId("user-1"), "Cyril")),
            repository.register(User(UserId("user-1"), "Cyril")),
        )
    }

    @Test
    fun `keeps the credential the server attached`() = runTest {
        val answered = cyril().copy(authenticatedId = "firebase-uid")
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse(payload = answered)))

        val result = repository.register(User(UserId("user-1"), "Cyril"))

        assertEquals("firebase-uid", (result as Result.Success).value.authenticatedId)
    }

    @Test
    fun `refuses an answer that carries neither a payload nor an error`() = runTest {
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse()))

        assertEquals(
            Result.Failure(RegisterUserError.EmptyResponse),
            repository.register(User(UserId("user-1"), "Cyril")),
        )
    }

    @Test
    fun `reports a refusal the server explains`() = runTest {
        val refused = ApiResponse<ApiUser>(error = ApiError(id = "whatever_the_server_sends"))
        val repository = UserRepositoryImpl(RecordingUserApi(refused))

        assertEquals(
            Result.Failure(RegisterUserError.Unknown),
            repository.register(User(UserId("user-1"), "Cyril")),
        )
    }

    @Test
    fun `names each field an answer left out`() {
        val incomplete = listOf(
            UserField.ID to cyril().copy(id = null),
            UserField.DISPLAY_NAME to cyril().copy(displayName = null),
        )

        incomplete.forEach { (missing, payload) ->
            assertEquals(
                Result.Failure(RegisterUserError.IncompleteResponse(missing)),
                payload.toUser(),
                "an answer without $missing should name it",
            )
        }
    }

    @Test
    fun `carries the incomplete answer through to the caller`() = runTest {
        val incomplete = ApiResponse(payload = cyril().copy(displayName = null))
        val repository = UserRepositoryImpl(RecordingUserApi(incomplete))

        assertEquals(
            Result.Failure(RegisterUserError.IncompleteResponse(UserField.DISPLAY_NAME)),
            repository.register(User(UserId("user-1"), "Cyril")),
        )
    }

    @Test
    fun `turns a transport failure into a domain error`() = runTest {
        val repository = UserRepositoryImpl(ThrowingUserApi(IllegalStateException("no network")))

        assertEquals(
            Result.Failure(RegisterUserError.Unknown),
            repository.register(User(UserId("user-1"), "Cyril")),
        )
    }

    @Test
    fun `lets a cancellation through instead of reporting it as a failure`() = runTest {
        val repository = UserRepositoryImpl(ThrowingUserApi(CancellationException("cancelled")))

        assertFailsWith<CancellationException> {
            repository.register(User(UserId("user-1"), "Cyril"))
        }
    }

    @Test
    fun `observes a user the api knows`() = runTest {
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse(), listOf(cyril())))

        assertEquals(User(UserId("user-1"), "Cyril"), repository.observeUser(UserId("user-1")).first())
    }

    @Test
    fun `observes null for a user the api does not know`() = runTest {
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse()))

        assertNull(repository.observeUser(UserId("nobody")).first())
    }

    @Test
    fun `asks the api only for the identifiers it was given`() = runTest {
        val api = RecordingUserApi(ApiResponse(), listOf(cyril()))

        UserRepositoryImpl(api).observeUsers(setOf(UserId("user-1"), UserId("user-2"))).first()

        assertEquals(setOf("user-1", "user-2"), api.lastIds)
    }

    @Test
    fun `drops a user the answer could not describe rather than failing the stream`() = runTest {
        val answered = listOf(cyril(), ApiUser(id = "user-2"))
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse(), answered))

        assertEquals(
            listOf(User(UserId("user-1"), "Cyril")),
            repository.observeUsers(setOf(UserId("user-1"), UserId("user-2"))).first(),
        )
    }

    private fun cyril() = ApiUser(id = "user-1", displayName = "Cyril")

    private class RecordingUserApi(
        private val response: ApiResponse<ApiUser>,
        private val known: List<ApiUser> = emptyList(),
    ) : UserApi {
        var lastRequest: ApiRegisterUserRequest? = null
            private set
        var lastIds: Set<String>? = null
            private set

        override suspend fun register(request: ApiRegisterUserRequest): ApiResponse<ApiUser> {
            lastRequest = request
            return response
        }

        override fun observeUsers(ids: Set<String>): Flow<List<ApiUser>> {
            lastIds = ids
            return flowOf(known.filter { it.id in ids })
        }
    }

    private class ThrowingUserApi(private val failure: Throwable) : UserApi {
        override suspend fun register(request: ApiRegisterUserRequest): ApiResponse<ApiUser> =
            throw failure

        override fun observeUsers(ids: Set<String>): Flow<List<ApiUser>> = flowOf(emptyList())
    }
}
