package com.cyrillrx.family.presentation.onboarding.joingroup

import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.InvitationRepository
import com.cyrillrx.family.group.domain.JoinGroupError
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.ACCEPTED_CODE
import com.cyrillrx.family.group.domain.SampleInvitationRepository.Companion.REVOKED_CODE
import com.cyrillrx.family.group.domain.UserRepository
import com.cyrillrx.family.presentation.AmnesicUserRepository
import com.cyrillrx.family.presentation.CountingInvitationRepository
import com.cyrillrx.family.presentation.groupRepositoryWithAGroup
import com.cyrillrx.family.presentation.registeredUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JoinGroupViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `opens on an empty code`() = runTest {
        assertEquals(JoinGroupState(), viewModel(registeredUserRepository()).state.value)
    }

    @Test
    fun `keeps the code that is typed`() = runTest {
        val viewModel = viewModel(registeredUserRepository())

        viewModel.changeInvitationCode(ACCEPTED_CODE)

        assertEquals(JoinGroupState(code = ACCEPTED_CODE), viewModel.state.value)
    }

    @Test
    fun `clears the refusal as soon as the code changes`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        viewModel.joinGroup()
        advanceUntilIdle()

        viewModel.changeInvitationCode("a")

        assertEquals(JoinGroupState(code = "a"), viewModel.state.value)
    }

    @Test
    fun `ignores what is typed while it redeems the code`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        viewModel.changeInvitationCode(ACCEPTED_CODE)
        viewModel.joinGroup()

        viewModel.changeInvitationCode("something else")

        assertEquals(
            JoinGroupState(code = ACCEPTED_CODE, submitting = true),
            viewModel.state.value,
        )
    }

    @Test
    fun `says it is working while it redeems the code`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.joinGroup()

        assertEquals(
            JoinGroupState(code = ACCEPTED_CODE, submitting = true),
            viewModel.state.value,
        )
    }

    @Test
    fun `announces the join once`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        val joins = collectJoins(viewModel)
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.joinGroup()
        advanceUntilIdle()

        assertEquals(1, joins.size)
    }

    @Test
    fun `stops working once joined`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.joinGroup()
        advanceUntilIdle()

        assertEquals(JoinGroupState(code = ACCEPTED_CODE), viewModel.state.value)
    }

    @Test
    fun `carries the refusal of a code too short`() = runTest {
        assertRefuses(code = "short", with = JoinGroupError.CodeTooShort)
    }

    @Test
    fun `carries the cause a revoked code gives`() = runTest {
        assertRefuses(
            code = REVOKED_CODE,
            with = JoinGroupError.Redemption(RedeemInvitationError.Revoked),
        )
    }

    @Test
    fun `carries the refusal when a group is already there`() = runTest {
        assertRefuses(
            code = ACCEPTED_CODE,
            with = JoinGroupError.AlreadyInAGroup,
            groupRepository = groupRepositoryWithAGroup(),
        )
    }

    @Test
    fun `carries the refusal when the registration was lost`() = runTest {
        val viewModel = viewModel(AmnesicUserRepository())
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.joinGroup()
        advanceUntilIdle()

        assertEquals(
            JoinGroupState(code = ACCEPTED_CODE, error = JoinGroupError.NotRegistered),
            viewModel.state.value,
        )
    }

    @Test
    fun `redeems once however many times the button is pressed`() = runTest {
        val invitations = CountingInvitationRepository()
        val viewModel = viewModel(registeredUserRepository(), invitationRepository = invitations)
        viewModel.changeInvitationCode(ACCEPTED_CODE)

        viewModel.joinGroup()
        viewModel.joinGroup()
        advanceUntilIdle()

        assertEquals(1, invitations.redemptions)
    }

    private suspend fun TestScope.assertRefuses(
        code: String,
        with: JoinGroupError,
        groupRepository: GroupRepository = RamGroupRepository(),
    ) {
        val viewModel = viewModel(registeredUserRepository(), groupRepository)
        viewModel.changeInvitationCode(code)

        viewModel.joinGroup()
        advanceUntilIdle()

        assertEquals(JoinGroupState(code = code, error = with), viewModel.state.value)
    }

    private fun TestScope.collectJoins(viewModel: JoinGroupViewModel): List<Unit> {
        val joins = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.joined.collect { joins += it }
        }

        return joins
    }

    private fun viewModel(
        userRepository: UserRepository,
        groupRepository: GroupRepository = RamGroupRepository(),
        invitationRepository: InvitationRepository = SampleInvitationRepository(),
    ) = JoinGroupViewModel(Onboarding(userRepository, groupRepository, invitationRepository))
}
