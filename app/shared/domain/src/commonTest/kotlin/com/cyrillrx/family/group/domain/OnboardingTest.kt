package com.cyrillrx.family.group.domain

import com.cyrillrx.core.api.ApiResponse
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.api.ApiRegisterUserRequest
import com.cyrillrx.family.group.api.ApiUser
import com.cyrillrx.family.group.data.UserApi
import com.cyrillrx.family.group.model.Group
import com.cyrillrx.family.group.model.GroupId
import com.cyrillrx.family.group.model.Invitation
import com.cyrillrx.family.group.model.InvitationId
import com.cyrillrx.family.group.model.Member
import com.cyrillrx.family.group.model.RedeemedInvitation
import com.cyrillrx.family.group.model.User
import com.cyrillrx.family.group.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class OnboardingTest {

    @Test
    fun `resumes on the display name when nobody is registered`() = runTest {
        assertEquals(Result.Success(OnboardingStep.DISPLAY_NAME), onboarding().currentStep())
    }

    @Test
    fun `resumes on the group choice once registered`() = runTest {
        val onboarding = onboarding()
        onboarding.register(displayName = "Cyril")

        assertEquals(Result.Success(OnboardingStep.GROUP_CHOICE), onboarding.currentStep())
    }

    @Test
    fun `cannot tell the step when the store cannot say who is registered`() = runTest {
        assertEquals(Result.Failure(UNREACHABLE), onboarding(UnavailableUserRepository()).currentStep())
    }

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

        assertEquals(Result.Success(Registration.Registered(UserId("user-1"))), users.registeredUserId())
    }

    @Test
    fun `reports an unreachable store rather than registering under a new identifier`() = runTest {
        assertEquals(
            Result.Failure(RegisterError.Registration(UNREACHABLE)),
            onboarding(UnavailableUserRepository()).register(displayName = "Cyril"),
        )
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
        val users = UserRepositoryImpl(EchoingUserApi())

        onboarding(users).register(" ")

        assertEquals(Result.Success(Registration.NotRegistered), users.registeredUserId())
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
            Result.Failure(RegisterError.Registration(RegisterUserError.Unknown)),
            onboarding(FailingUserRepository).register("Cyril"),
        )
    }

    @Test
    fun `creates a group for the registered user`() = runTest {
        val groups = RamGroupRepository()
        val onboarding = onboarding(groupRepository = groups)
        onboarding.register(displayName = "Cyril")

        val group = (onboarding.createGroup() as Result.Success).value

        assertEquals(GroupId("group-1"), group.id)
        assertEquals(Result.Success(Membership.InGroup(group)), groups.group())
        assertEquals(
            listOf(Member(userId = UserId("user-1"), groupId = group.id, joinedAt = NOW)),
            groups.observeMembers(group.id).first(),
        )
    }

    @Test
    fun `names the group without borrowing the founder's name`() = runTest {
        val onboarding = onboarding()
        onboarding.register(displayName = "Cyril")

        assertEquals("Family", (onboarding.createGroup() as Result.Success).value.name)
    }

    @Test
    fun `reports an unavailable store rather than nobody registered when founding a group`() = runTest {
        assertEquals(
            Result.Failure(CreateGroupError.Creation(UNREACHABLE)),
            onboarding(UnavailableUserRepository()).createGroup(),
        )
    }

    @Test
    fun `refuses to create a group before anyone is registered`() = runTest {
        assertEquals(Result.Failure(CreateGroupError.NotRegistered), onboarding().createGroup())
    }

    @Test
    fun `writes nothing when nobody is registered`() = runTest {
        val groups = RamGroupRepository()

        onboarding(groupRepository = groups).createGroup()

        assertEquals(Result.Success(Membership.NoGroup), groups.group())
    }

    @Test
    fun `refuses to create a second group`() = runTest {
        val onboarding = onboarding()
        onboarding.register(displayName = "Cyril")
        onboarding.createGroup()

        assertEquals(
            Result.Failure(CreateGroupError.Creation(AlreadyInAGroup)),
            onboarding.createGroup(),
        )
    }

    @Test
    fun `leaves the first group and its member untouched when a second is refused`() = runTest {
        val groups = RamGroupRepository()
        val onboarding = onboarding(groupRepository = groups)
        onboarding.register(displayName = "Cyril")
        val first = (onboarding.createGroup() as Result.Success).value

        onboarding.createGroup()

        assertEquals(Result.Success(Membership.InGroup(first)), groups.group())
        assertEquals(1, groups.observeMembers(first.id).first().size)
    }

    @Test
    fun `reports the refusal the repository met while writing`() = runTest {
        val onboarding = onboarding(groupRepository = RefusingGroupRepository(GroupNotFound))
        onboarding.register(displayName = "Cyril")

        assertEquals(Result.Failure(CreateGroupError.Creation(GroupNotFound)), onboarding.createGroup())
    }

    @Test
    fun `redeems nothing while this device holds a membership of a missing group`() = runTest {
        val invitations = RecordingInvitationRepository()
        val onboarding = onboarding(groupRepository = MissingGroupRepository(), invitationRepository = invitations)
        onboarding.register(displayName = "Cyril")

        assertEquals(Result.Failure(JoinGroupError.Lookup(GroupNotFound)), onboarding.joinGroup(CODE))
        assertEquals(0, invitations.calls)
    }

    @Test
    fun `leaves a missing group`() = runTest {
        val groups = MissingGroupRepository()

        onboarding(groupRepository = groups).leaveMissingGroup()

        assertEquals(1, groups.departures)
    }

    @Test
    fun `joins the group behind a valid invitation`() = runTest {
        val onboarding = onboarding()
        onboarding.register(displayName = "Cyril")

        assertEquals(
            Result.Success(GroupId("group-of-the-inviter")),
            onboarding.joinGroup(CODE),
        )
    }

    @Test
    fun `reports an unavailable store rather than nobody registered when joining`() = runTest {
        assertEquals(
            Result.Failure(JoinGroupError.Lookup(UNREACHABLE)),
            onboarding(UnavailableUserRepository()).joinGroup(CODE),
        )
    }

    @Test
    fun `redeems nothing when it cannot tell whether this device already has a group`() = runTest {
        val invitations = RecordingInvitationRepository()
        val onboarding = onboarding(groupRepository = UnavailableGroupRepository(), invitationRepository = invitations)
        onboarding.register(displayName = "Cyril")

        assertEquals(Result.Failure(JoinGroupError.Lookup(UNREACHABLE)), onboarding.joinGroup(CODE))
        assertEquals(0, invitations.calls)
    }

    @Test
    fun `refuses to join before anyone is registered`() = runTest {
        assertEquals(Result.Failure(JoinGroupError.NotRegistered), onboarding().joinGroup(CODE))
    }

    @Test
    fun `refuses a code too short to be an invitation without calling the service`() = runTest {
        val invitations = RecordingInvitationRepository()
        val onboarding = onboarding(invitationRepository = invitations)
        onboarding.register(displayName = "Cyril")

        assertEquals(Result.Failure(JoinGroupError.CodeTooShort), onboarding.joinGroup("too-short"))
        assertEquals(0, invitations.calls)
    }

    @Test
    fun `trims the invitation code`() = runTest {
        val invitations = RecordingInvitationRepository()
        val onboarding = onboarding(invitationRepository = invitations)
        onboarding.register(displayName = "Cyril")

        onboarding.joinGroup("  $CODE\n")

        assertEquals(CODE, invitations.lastCode)
    }

    @Test
    fun `refuses a code only long enough because of its padding`() = runTest {
        val invitations = RecordingInvitationRepository()
        val onboarding = onboarding(invitationRepository = invitations)
        onboarding.register(displayName = "Cyril")

        assertEquals(
            Result.Failure(JoinGroupError.CodeTooShort),
            onboarding.joinGroup("a".padEnd(Invitation.MIN_CODE_LENGTH)),
        )
        assertEquals(0, invitations.calls)
    }

    @Test
    fun `refuses to join when this device already has a group`() = runTest {
        val invitations = RecordingInvitationRepository()
        val onboarding = onboarding(invitationRepository = invitations)
        onboarding.register(displayName = "Cyril")
        onboarding.createGroup()

        assertEquals(Result.Failure(JoinGroupError.AlreadyInAGroup), onboarding.joinGroup(CODE))
        assertEquals(0, invitations.calls)
    }

    @Test
    fun `surfaces the reason a redemption was refused`() = runTest {
        val onboarding = onboarding(
            invitationRepository = RefusingInvitationRepository(RedeemInvitationError.Expired),
        )
        onboarding.register(displayName = "Cyril")

        assertEquals(
            Result.Failure(JoinGroupError.Redemption(RedeemInvitationError.Expired)),
            onboarding.joinGroup(CODE),
        )
    }

    @Test
    fun `writes no membership itself`() = runTest {
        val groups = RamGroupRepository()
        val onboarding = onboarding(groupRepository = groups)
        onboarding.register(displayName = "Cyril")

        onboarding.joinGroup(CODE)

        assertEquals(emptyList(), groups.observeMembers(GroupId("group-of-the-inviter")).first())
    }

    private fun onboarding(
        userRepository: UserRepository = UserRepositoryImpl(EchoingUserApi()),
        groupRepository: GroupRepository = RamGroupRepository(),
        invitationRepository: InvitationRepository = SilentInvitationRepository,
    ): Onboarding {
        // One generator for both, as the production default does: the factory derives its own from it.
        val ids = CountingIdGenerator()

        return Onboarding(
            userRepository,
            groupRepository,
            invitationRepository,
            idGenerator = ids,
            groupFactory = GroupFactory(ids, FixedClock),
        )
    }

    /** Redeems without writing anything, so a caller that relies on the write shows up. */
    private object SilentInvitationRepository : InvitationRepository {
        override suspend fun redeem(code: String, user: UserId) = Result.Success(
            RedeemedInvitation(
                id = InvitationId("invitation-1"),
                groupId = GroupId("group-of-the-inviter"),
                code = code,
                createdAt = Instant.fromEpochMilliseconds(epochMilliseconds = 0),
                redeemedBy = user,
                redeemedAt = NOW,
            ),
        )
    }

    private class RecordingInvitationRepository : InvitationRepository {
        var calls = 0
            private set

        var lastCode: String? = null
            private set

        override suspend fun redeem(code: String, user: UserId) =
            SilentInvitationRepository.redeem(code, user).also {
                calls++
                lastCode = code
            }
    }

    private class RefusingInvitationRepository(
        private val reason: RedeemInvitationError,
    ) : InvitationRepository {
        override suspend fun redeem(code: String, user: UserId) = Result.Failure(reason)
    }

    private class RefusingGroupRepository(private val error: GroupError.Write) : GroupRepository {
        override suspend fun group() = Result.Success(Membership.NoGroup)

        override fun observeMembers(groupId: GroupId): Flow<List<Member>> = emptyFlow()

        override suspend fun create(group: Group, founder: UserId) = Result.Failure(error)

        override suspend fun leaveMissingGroup() = Result.Success(Unit)
    }

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
        override suspend fun registeredUserId() = Result.Success(Registration.NotRegistered)

        override suspend fun register(user: User) = Result.Failure(RegisterUserError.Unknown)
    }

    private companion object {
        val NOW: Instant = Instant.fromEpochMilliseconds(1_500)
        val CODE = "a".repeat(Invitation.MIN_CODE_LENGTH)
    }
}
