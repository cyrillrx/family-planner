package com.cyrillrx.family.group.domain

import com.cyrillrx.core.data.model.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.data.UserApi
import com.cyrillrx.family.group.data.model.ApiRegisterUserRequest
import com.cyrillrx.family.group.data.model.ApiUser
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.InvitationId
import com.cyrillrx.family.group.domain.model.Member
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

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

    @Test
    fun `creates a group for the registered user`() = runTest {
        val groups = RamGroupRepository()
        val onboarding = onboarding(groupRepository = groups)
        onboarding.register("Cyril")

        val group = (onboarding.createGroup() as Result.Success).value

        assertEquals(GroupId("group-1"), group.id)
        assertEquals(group, groups.group())
        assertEquals(
            listOf(Member(userId = UserId("user-1"), groupId = group.id, joinedAt = NOW)),
            groups.observeMembers(group.id).first(),
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
        assertEquals(Result.Failure(CreateGroupError.NotRegistered), onboarding().createGroup())
    }

    @Test
    fun `writes nothing when nobody is registered`() = runTest {
        val groups = RamGroupRepository()

        onboarding(groupRepository = groups).createGroup()

        assertNull(groups.group())
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

        assertEquals(first, groups.group())
        assertEquals(1, groups.observeMembers(first.id).first().size)
    }

    private fun onboarding(
        userRepository: UserRepository = UserRepositoryImpl(EchoingUserApi()),
        groupRepository: GroupRepository = RamGroupRepository(),
    ) = Onboarding(
        userRepository,
        groupRepository,
        groupFactory = GroupFactory(CountingIdGenerator(), FixedClock),
        idGenerator = CountingIdGenerator(),
    )

    private object FixedClock : Clock {
        override fun now(): Instant = NOW
    }

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

    private companion object {
        val NOW: Instant = Instant.fromEpochMilliseconds(1_500)
    }
}
