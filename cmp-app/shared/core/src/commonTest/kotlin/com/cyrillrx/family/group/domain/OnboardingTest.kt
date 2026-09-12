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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
        val store = RamCurrentUserStore()

        onboarding(currentUserStore = store).register("Cyril")

        assertEquals(UserId("user-1"), store.observeCurrentUserId().first())
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
            Result.Failure(RegisterError.BlankDisplayName),
            onboarding().register(""),
        )
    }

    @Test
    fun `rejects a display name of only whitespace`() = runTest {
        assertEquals(
            Result.Failure(RegisterError.BlankDisplayName),
            onboarding().register("   "),
        )
    }

    @Test
    fun `remembers nobody when the display name is rejected`() = runTest {
        val store = RamCurrentUserStore()

        onboarding(currentUserStore = store).register(" ")

        assertNull(store.observeCurrentUserId().first())
    }

    @Test
    fun `reuses the identifier this device already has`() = runTest {
        val api = EchoingUserApi()
        val store = RamCurrentUserStore(UserId("user-from-a-previous-run"))

        onboarding(UserRepositoryImpl(api), store).register("Cyril")

        assertEquals("user-from-a-previous-run", api.lastRequest?.id)
    }

    @Test
    fun `reports a registration failure`() = runTest {
        assertEquals(
            Result.Failure(RegisterError.Failed(RegisterUserError.Unknown)),
            onboarding(FailingUserRepository).register("Cyril"),
        )
    }

    @Test
    fun `remembers nobody when registration fails`() = runTest {
        val store = RamCurrentUserStore()

        onboarding(FailingUserRepository, store).register("Cyril")

        assertNull(store.observeCurrentUserId().first())
    }

    private fun onboarding(
        userRepository: UserRepository = UserRepositoryImpl(EchoingUserApi()),
        currentUserStore: CurrentUserStore = RamCurrentUserStore(),
    ) = Onboarding(userRepository, currentUserStore, ids = CountingIdGenerator())

    /** Answers with whatever it was asked to register, as the real service would. */
    private class EchoingUserApi : UserApi {
        var lastRequest: ApiRegisterUserRequest? = null
            private set

        override suspend fun register(request: ApiRegisterUserRequest): ApiResponse<ApiUser> {
            lastRequest = request
            return ApiResponse(payload = ApiUser(id = request.id, displayName = request.displayName))
        }

        override fun observeUsers(ids: Set<String>): Flow<List<ApiUser>> = flowOf(emptyList())
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
        override fun observeUser(id: UserId): Flow<User?> = flowOf(null)

        override fun observeUsers(ids: Set<UserId>): Flow<List<User>> = flowOf(emptyList())

        override suspend fun register(user: User) = Result.Failure(RegisterUserError.Unknown)
    }
}
