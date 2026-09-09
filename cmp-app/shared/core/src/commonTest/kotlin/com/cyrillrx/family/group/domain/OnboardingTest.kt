package com.cyrillrx.family.group.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.ApiUserRepository
import com.cyrillrx.family.group.data.RamCurrentUserStore
import com.cyrillrx.family.group.data.RamGroupRepository
import com.cyrillrx.family.group.data.RamUserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

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

    @Test
    fun `creates a group for the registered user`() = runTest {
        val groups = RamGroupRepository()
        val onboarding = onboarding(groupRepository = groups)
        onboarding.register("Cyril")

        val result = onboarding.createGroup()

        val group = (result as Result.Success).value
        assertEquals(GroupId("group-1"), group.id)
        assertEquals(group, groups.observeGroup().first())
        assertEquals(
            listOf(Member(userId = UserId("user-1"), groupId = group.id, joinedAt = NOW)),
            groups.observeMembers().first(),
        )
    }

    @Test
    fun `names the group without borrowing the founder's name`() = runTest {
        val onboarding = onboarding()
        onboarding.register("Cyril")

        assertEquals("Family", (onboarding.createGroup() as Result.Success).value.name)
    }

    @Test
    fun `refuses to create a group before anyone is registered`() = runTest {
        assertEquals(
            Result.Failure(CreateGroupError.NotRegistered),
            onboarding().createGroup(),
        )
    }

    @Test
    fun `writes nothing when nobody is registered`() = runTest {
        val groups = RamGroupRepository()

        onboarding(groupRepository = groups).createGroup()

        assertNull(groups.observeGroup().first())
        assertEquals(emptyList(), groups.observeMembers().first())
    }

    @Test
    fun `refuses to create a second group`() = runTest {
        val onboarding = onboarding()
        onboarding.register("Cyril")
        onboarding.createGroup()

        assertEquals(
            Result.Failure(CreateGroupError.GroupAlreadyExists),
            onboarding.createGroup(),
        )
    }

    @Test
    fun `leaves the first group and its member untouched when a second is refused`() = runTest {
        val groups = RamGroupRepository()
        val onboarding = onboarding(groupRepository = groups)
        onboarding.register("Cyril")
        val first = (onboarding.createGroup() as Result.Success).value

        onboarding.createGroup()

        assertEquals(first, groups.observeGroup().first())
        assertEquals(1, groups.observeMembers().first().size)
    }

    private fun onboarding(
        userRepository: UserRepository = ApiUserRepository(RamUserApi()),
        currentUserStore: CurrentUserStore = RamCurrentUserStore(),
        groupRepository: GroupRepository = RamGroupRepository(),
    ) = Onboarding(
        groupRepository,
        userRepository,
        currentUserStore,
        groupFactory = GroupFactory(ids = CountingIdGenerator(), clock = FixedClock),
        ids = CountingIdGenerator(),
    )

    private object FixedClock : Clock {
        override fun now(): Instant = NOW
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

    private companion object {
        val NOW: Instant = Instant.fromEpochMilliseconds(1_500)
    }
}
