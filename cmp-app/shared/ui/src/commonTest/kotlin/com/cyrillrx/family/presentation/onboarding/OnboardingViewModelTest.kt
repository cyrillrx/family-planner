package com.cyrillrx.family.presentation.onboarding

import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.InvitationField
import com.cyrillrx.family.group.domain.InvitationRepository
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RegisterUserError
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.ACCEPTED_CODE
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.ALREADY_REDEEMED_CODE
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.EXPIRED_CODE
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.REVOKED_CODE
import com.cyrillrx.family.group.domain.UserRepository
import com.cyrillrx.family.group.domain.model.Group
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.group.domain.model.User
import com.cyrillrx.family.group.domain.model.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class OnboardingViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // region The name step

    @Test
    fun `opens by asking for a name`() = runTest {
        assertEquals(OnboardingState.Name(), viewModel().state.value)
    }

    @Test
    fun `keeps what is typed`() = runTest {
        val viewModel = viewModel()

        viewModel.changeDisplayName("Alice")

        assertEquals(OnboardingState.Name(displayName = "Alice"), viewModel.state.value)
    }

    @Test
    fun `clears the refusal as soon as the name changes`() = runTest {
        val viewModel = viewModel()
        viewModel.submitDisplayName()
        advanceUntilIdle()

        viewModel.changeDisplayName("A")

        assertEquals(OnboardingState.Name(displayName = "A"), viewModel.state.value)
    }

    @Test
    fun `says it is working while it registers`() = runTest {
        val viewModel = viewModel()
        viewModel.changeDisplayName("Alice")

        viewModel.submitDisplayName()

        assertEquals(
            OnboardingState.Name(displayName = "Alice", submitting = true),
            viewModel.state.value,
        )
    }

    @Test
    fun `moves on to the choice once registered`() = runTest {
        val viewModel = viewModel()
        viewModel.changeDisplayName("Alice")

        viewModel.submitDisplayName()
        advanceUntilIdle()

        assertEquals(OnboardingState.Choice(), viewModel.state.value)
    }

    @Test
    fun `refuses a blank name`() = runTest {
        val viewModel = viewModel()

        viewModel.submitDisplayName()
        advanceUntilIdle()

        assertEquals(
            OnboardingState.Name(error = OnboardingError.BlankDisplayName),
            viewModel.state.value,
        )
    }

    @Test
    fun `surfaces a failed registration as unexpected`() = runTest {
        val viewModel = viewModel(userRepository = FailingUserRepository)
        viewModel.changeDisplayName("Alice")

        viewModel.submitDisplayName()
        advanceUntilIdle()

        assertEquals(
            OnboardingState.Name(displayName = "Alice", error = OnboardingError.Unexpected),
            viewModel.state.value,
        )
    }

    @Test
    fun `registers once however many times the button is pressed`() = runTest {
        val users = CountingUserRepository()
        val viewModel = viewModel(userRepository = users)
        viewModel.changeDisplayName("Alice")

        viewModel.submitDisplayName()
        viewModel.submitDisplayName()
        advanceUntilIdle()

        assertEquals(1, users.registrations)
    }

    @Test
    fun `ignores what is typed while it registers`() = runTest {
        val viewModel = viewModel()
        viewModel.changeDisplayName("Alice")
        viewModel.submitDisplayName()

        viewModel.changeDisplayName("Bob")

        assertEquals(
            OnboardingState.Name(displayName = "Alice", submitting = true),
            viewModel.state.value,
        )
    }

    // endregion

    // region The choice step

    @Test
    fun `creates a group and lands in it`() = runTest {
        val viewModel = atChoice()

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(OnboardingState.Done(groupName = "Family"), viewModel.state.value)
    }

    @Test
    fun `says it is working while it creates the group`() = runTest {
        val viewModel = atChoice()

        viewModel.createGroup()

        assertEquals(OnboardingState.Choice(submitting = true), viewModel.state.value)
    }

    @Test
    fun `refuses to found a second group`() = runTest {
        val viewModel = atChoice(groupRepository = RamGroupRepository().withAGroup())

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(
            OnboardingState.Choice(error = OnboardingError.AlreadyInAGroup),
            viewModel.state.value,
        )
    }

    @Test
    fun `surfaces a lost registration as unexpected when creating`() = runTest {
        val viewModel = atChoice(userRepository = AmnesicUserRepository())

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(
            OnboardingState.Choice(error = OnboardingError.Unexpected),
            viewModel.state.value,
        )
    }

    @Test
    fun `opens the join step`() = runTest {
        val viewModel = atChoice()

        viewModel.openJoinGroup()

        assertEquals(OnboardingState.Join(), viewModel.state.value)
    }

    @Test
    fun `ignores the join step while it creates the group`() = runTest {
        val viewModel = atChoice()
        viewModel.createGroup()

        viewModel.openJoinGroup()

        assertEquals(OnboardingState.Choice(submitting = true), viewModel.state.value)
    }

    @Test
    fun `creates one group however many times the button is pressed`() = runTest {
        val groups = RamGroupRepository()
        val viewModel = atChoice(groupRepository = groups)

        viewModel.createGroup()
        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(OnboardingState.Done(groupName = "Family"), viewModel.state.value)
    }

    // endregion

    // region The join step

    @Test
    fun `keeps the code that is typed`() = runTest {
        val viewModel = atJoin()

        viewModel.changeInvitationCode(ACCEPTED_CODE)

        assertEquals(OnboardingState.Join(code = ACCEPTED_CODE), viewModel.state.value)
    }

    @Test
    fun `clears the refusal as soon as the code changes`() = runTest {
        val viewModel = atJoin()
        viewModel.submitInvitationCode()
        advanceUntilIdle()

        viewModel.changeInvitationCode("a")

        assertEquals(OnboardingState.Join(code = "a"), viewModel.state.value)
    }

    @Test
    fun `goes back to the choice`() = runTest {
        val viewModel = atJoin()

        viewModel.backToChoice()

        assertEquals(OnboardingState.Choice(), viewModel.state.value)
    }

    @Test
    fun `joins the group and waits for it to arrive`() = runTest {
        val viewModel = atJoin()
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.submitInvitationCode()
        advanceUntilIdle()

        assertEquals(OnboardingState.Done(groupName = null), viewModel.state.value)
    }

    @Test
    fun `says it is working while it redeems the code`() = runTest {
        val viewModel = atJoin()
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.submitInvitationCode()

        assertEquals(
            OnboardingState.Join(code = ACCEPTED_CODE, submitting = true),
            viewModel.state.value,
        )
    }

    @Test
    fun `refuses a code too short to be an invitation`() = runTest {
        assertRefuses(code = "short", with = OnboardingError.CodeTooShort)
    }

    @Test
    fun `surfaces a revoked code`() = runTest {
        assertRefuses(code = REVOKED_CODE, with = OnboardingError.CodeRevoked)
    }

    @Test
    fun `surfaces a code already used`() = runTest {
        assertRefuses(code = ALREADY_REDEEMED_CODE, with = OnboardingError.CodeAlreadyRedeemed)
    }

    @Test
    fun `surfaces an expired code`() = runTest {
        assertRefuses(code = EXPIRED_CODE, with = OnboardingError.CodeExpired)
    }

    @Test
    fun `surfaces a code it does not know as unexpected`() = runTest {
        assertRefuses(code = "unknown-invitation-001", with = OnboardingError.Unexpected)
    }

    @Test
    fun `surfaces an empty response as unexpected`() = runTest {
        assertRefuses(
            code = ACCEPTED_CODE,
            with = OnboardingError.Unexpected,
            invitationRepository = RefusingInvitationRepository(RedeemInvitationError.EmptyResponse),
        )
    }

    @Test
    fun `surfaces an incomplete response as unexpected`() = runTest {
        assertRefuses(
            code = ACCEPTED_CODE,
            with = OnboardingError.Unexpected,
            invitationRepository = RefusingInvitationRepository(
                RedeemInvitationError.IncompleteResponse(InvitationField.GROUP_ID),
            ),
        )
    }

    @Test
    fun `refuses to join when a group is already there`() = runTest {
        val viewModel = atJoin(groupRepository = RamGroupRepository().withAGroup())
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.submitInvitationCode()
        advanceUntilIdle()

        assertEquals(
            OnboardingState.Join(code = ACCEPTED_CODE, error = OnboardingError.AlreadyInAGroup),
            viewModel.state.value,
        )
    }

    @Test
    fun `surfaces a lost registration as unexpected when joining`() = runTest {
        val viewModel = atJoin(userRepository = AmnesicUserRepository())
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.submitInvitationCode()
        advanceUntilIdle()

        assertEquals(
            OnboardingState.Join(code = ACCEPTED_CODE, error = OnboardingError.Unexpected),
            viewModel.state.value,
        )
    }

    @Test
    fun `redeems once however many times the button is pressed`() = runTest {
        val invitations = CountingInvitationRepository()
        val viewModel = atJoin(invitationRepository = invitations)
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.submitInvitationCode()
        viewModel.submitInvitationCode()
        advanceUntilIdle()

        assertEquals(1, invitations.redemptions)
    }

    @Test
    fun `ignores the back button while it redeems the code`() = runTest {
        val viewModel = atJoin()
        viewModel.changeInvitationCode(ACCEPTED_CODE)
        viewModel.submitInvitationCode()

        viewModel.backToChoice()

        assertEquals(
            OnboardingState.Join(code = ACCEPTED_CODE, submitting = true),
            viewModel.state.value,
        )
    }

    @Test
    fun `ignores what is typed while it redeems the code`() = runTest {
        val viewModel = atJoin()
        viewModel.changeInvitationCode(ACCEPTED_CODE)
        viewModel.submitInvitationCode()

        viewModel.changeInvitationCode("something else")

        assertEquals(
            OnboardingState.Join(code = ACCEPTED_CODE, submitting = true),
            viewModel.state.value,
        )
    }

    // endregion

    // region Actions that do not belong to the step on screen

    @Test
    fun `ignores the name actions once the name is behind us`() = runTest {
        val viewModel = atChoice()

        viewModel.changeDisplayName("Bob")
        viewModel.submitDisplayName()
        advanceUntilIdle()

        assertEquals(OnboardingState.Choice(), viewModel.state.value)
    }

    @Test
    fun `ignores the choice actions before the choice is reached`() = runTest {
        val viewModel = viewModel()

        viewModel.createGroup()
        viewModel.openJoinGroup()
        advanceUntilIdle()

        assertEquals(OnboardingState.Name(), viewModel.state.value)
    }

    @Test
    fun `ignores the join actions before the join step is reached`() = runTest {
        val viewModel = viewModel()

        viewModel.changeInvitationCode(ACCEPTED_CODE)
        viewModel.submitInvitationCode()
        viewModel.backToChoice()
        advanceUntilIdle()

        assertEquals(OnboardingState.Name(), viewModel.state.value)
    }

    // endregion

    private fun TestScope.assertRefuses(
        code: String,
        with: OnboardingError,
        invitationRepository: InvitationRepository = SampleInvitationRepository(),
    ) {
        val viewModel = atJoin(invitationRepository = invitationRepository)
        viewModel.changeInvitationCode(code)

        viewModel.submitInvitationCode()
        advanceUntilIdle()

        assertEquals(OnboardingState.Join(code = code, error = with), viewModel.state.value)
    }

    private fun viewModel(
        userRepository: UserRepository = RamUserRepository(),
        groupRepository: GroupRepository = RamGroupRepository(),
        invitationRepository: InvitationRepository = SampleInvitationRepository(),
    ) = OnboardingViewModel(Onboarding(userRepository, groupRepository, invitationRepository))

    private fun TestScope.atChoice(
        userRepository: UserRepository = RamUserRepository(),
        groupRepository: GroupRepository = RamGroupRepository(),
        invitationRepository: InvitationRepository = SampleInvitationRepository(),
    ): OnboardingViewModel {
        val viewModel = viewModel(userRepository, groupRepository, invitationRepository)
        viewModel.changeDisplayName("Alice")
        viewModel.submitDisplayName()
        advanceUntilIdle()

        return viewModel
    }

    private fun TestScope.atJoin(
        userRepository: UserRepository = RamUserRepository(),
        groupRepository: GroupRepository = RamGroupRepository(),
        invitationRepository: InvitationRepository = SampleInvitationRepository(),
    ): OnboardingViewModel {
        val viewModel = atChoice(userRepository, groupRepository, invitationRepository)
        viewModel.openJoinGroup()

        return viewModel
    }

    private suspend fun RamGroupRepository.withAGroup() = apply {
        setGroup(Group(id = GroupId("group-1"), name = "Home", createdAt = NOW))
    }

    /** Registers, then forgets — the founder guard then reads what a lost write would leave. */
    private class AmnesicUserRepository : UserRepository {
        override suspend fun registeredUserId(): UserId? = null

        override suspend fun register(user: User) = Result.Success(user)
    }

    private object FailingUserRepository : UserRepository {
        override suspend fun registeredUserId(): UserId? = null

        override suspend fun register(user: User) = Result.Failure(RegisterUserError.Unknown)
    }

    private class CountingUserRepository : UserRepository {
        var registrations = 0
            private set

        private val delegate = RamUserRepository()

        override suspend fun registeredUserId() = delegate.registeredUserId()

        override suspend fun register(user: User) = delegate.register(user).also { registrations++ }
    }

    private class CountingInvitationRepository : InvitationRepository {
        var redemptions = 0
            private set

        private val delegate = SampleInvitationRepository()

        override suspend fun redeem(code: String, user: UserId) =
            delegate.redeem(code, user).also { redemptions++ }
    }

    private class RefusingInvitationRepository(
        private val reason: RedeemInvitationError,
    ) : InvitationRepository {
        override suspend fun redeem(code: String, user: UserId) = Result.Failure(reason)
    }

    private companion object {
        val NOW: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
