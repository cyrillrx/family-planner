package com.cyrillrx.family.group.domain

import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.UserApi
import com.cyrillrx.family.group.data.model.ApiRegisterUserRequest
import com.cyrillrx.family.group.data.model.ApiUser
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.InvitationId
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OnboardingTest {

    @Test
    fun `registers the user of this device under the given name`() = runTest {
        val users = UserRepositoryImpl(EchoingUserApi())

        val result = onboarding(users).register("Cyril")

        val user = (result as Result.Success).value
        assertEquals(UserId("user-1"), user.id)
        assertEquals("Cyril", user.displayName)
    }

    @Test
    fun `remembers which user this device is`() = runTest {
        val users = UserRepositoryImpl(EchoingUserApi())

        onboarding(users).register("Cyril")

        assertEquals(UserId("user-1"), users.registeredUserId())
    }

    @Test
    fun `trims the display name`() = runTest {
        val api = EchoingUserApi()

        onboarding(UserRepositoryImpl(api)).register("  Cyril  ")

        assertEquals("Cyril", api.lastRequest?.displayName)
    }

    @Test
    fun `rejects a blank display name`() = runTest {
        assertEquals(
            Result.Failure(RegisterUserError.BlankDisplayName),
            onboarding().register(""),
        )
    }

    @Test
    fun `rejects a display name of only whitespace`() = runTest {
        assertEquals(
            Result.Failure(RegisterUserError.BlankDisplayName),
            onboarding().register("   "),
        )
    }

    @Test
    fun `remembers nobody when the display name is rejected`() = runTest {
        val users = UserRepositoryImpl(EchoingUserApi())

        onboarding(users).register(" ")

        assertNull(users.registeredUserId())
    }

    @Test
    fun `reuses the identifier this device already has`() = runTest {
        val api = EchoingUserApi()
        val users = UserRepositoryImpl(api, UserId("user-from-a-previous-run"))

        onboarding(users).register("Cyril")

        assertEquals("user-from-a-previous-run", api.lastRequest?.id)
    }

    @Test
    fun `reports a registration failure`() = runTest {
        assertEquals(
            Result.Failure(RegisterUserError.Unknown),
            onboarding(FailingUserRepository).register("Cyril"),
        )
    }

    private fun onboarding(
        userRepository: UserRepository = UserRepositoryImpl(EchoingUserApi()),
    ) = Onboarding(userRepository, idGenerator = CountingIdGenerator())

    /** Answers with whatever it was asked to register, as the real service would. */
    private class EchoingUserApi : UserApi {
        var lastRequest: ApiRegisterUserRequest? = null
            private set

        override suspend fun register(request: ApiRegisterUserRequest): ApiResponse<ApiUser> {
            lastRequest = request
            return ApiResponse(payload = ApiUser(id = request.id, displayName = request.displayName))
        }
    }

    private class CountingIdGenerator : IdGenerator {
        private var groups = 0
        private var users = 0
        private var invitations = 0

        override fun newGroupId() = GroupId("group-${++groups}")

        override fun newUserId() = UserId("user-${++users}")

        override fun newInvitationId() = InvitationId("invitation-${++invitations}")
    }

    private object FailingUserRepository : UserRepository {
        override suspend fun registeredUserId(): UserId? = null

        override suspend fun register(user: User) = Result.Failure(RegisterUserError.Unknown)
    }
}
