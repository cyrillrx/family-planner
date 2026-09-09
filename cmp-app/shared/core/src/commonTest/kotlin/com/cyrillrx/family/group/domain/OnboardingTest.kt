package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.ApiUserRepository
import com.cyrillrx.family.group.data.RamCurrentUserStore
import com.cyrillrx.family.group.data.RamUserApi
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
        val users = ApiUserRepository(RamUserApi())

        val result = onboarding(users).register("Cyril")

        val user = (result as Result.Success).value
        assertEquals(UserId("user-1"), user.id)
        assertEquals("Cyril", user.displayName)
        assertEquals(user, users.observeUser(UserId("user-1")).first())
    }

    @Test
    fun `remembers which user this device is`() = runTest {
        val store = RamCurrentUserStore()

        onboarding(currentUserStore = store).register("Cyril")

        assertEquals(UserId("user-1"), store.observeCurrentUserId().first())
    }

    @Test
    fun `trims the display name`() = runTest {
        val users = ApiUserRepository(RamUserApi())

        onboarding(users).register("  Cyril  ")

        assertEquals("Cyril", users.observeUser(UserId("user-1")).first()?.displayName)
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
        val users = ApiUserRepository(RamUserApi())
        val store = RamCurrentUserStore(UserId("user-from-a-previous-run"))

        onboarding(users, store).register("Cyril")

        assertEquals(
            "Cyril",
            users.observeUser(UserId("user-from-a-previous-run")).first()?.displayName,
        )
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
        userRepository: UserRepository = ApiUserRepository(RamUserApi()),
        currentUserStore: CurrentUserStore = RamCurrentUserStore(),
    ) = Onboarding(userRepository, currentUserStore, ids = CountingIdGenerator())

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
