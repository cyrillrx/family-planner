package com.cyrillrx.family.group.domain

import com.cyrillrx.core.data.model.ApiError
import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.UserApi
import com.cyrillrx.family.group.data.model.ApiRegisterUserRequest
import com.cyrillrx.family.group.data.model.ApiUser
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
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
    fun `knows nobody until someone registers`() = runTest {
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse()))

        assertNull(repository.registeredUserId())
    }

    @Test
    fun `makes the registered user this device's own`() = runTest {
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse(payload = cyril())))

        repository.register(User(UserId("user-1"), "Cyril"))

        assertEquals(UserId("user-1"), repository.registeredUserId())
    }

    @Test
    fun `keeps nobody when registration is refused`() = runTest {
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse()))

        repository.register(User(UserId("user-1"), "Cyril"))

        assertNull(repository.registeredUserId())
    }

    @Test
    fun `starts from the user it was built with`() = runTest {
        val repository = UserRepositoryImpl(RecordingUserApi(ApiResponse()), UserId("from-last-run"))

        assertEquals(UserId("from-last-run"), repository.registeredUserId())
    }

    private fun cyril() = ApiUser(id = "user-1", displayName = "Cyril")

    private class RecordingUserApi(private val response: ApiResponse<ApiUser>) : UserApi {
        var lastRequest: ApiRegisterUserRequest? = null
            private set

        override suspend fun register(request: ApiRegisterUserRequest): ApiResponse<ApiUser> {
            lastRequest = request
            return response
        }
    }

    private class ThrowingUserApi(private val failure: Throwable) : UserApi {
        override suspend fun register(request: ApiRegisterUserRequest): ApiResponse<ApiUser> =
            throw failure
    }
}
