package com.cyrillrx.family.presentation.onboarding.groupchoice

import com.cyrillrx.family.group.domain.CreateGroupError
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.UserRepository
import com.cyrillrx.family.presentation.AmnesicUserRepository
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

class GroupChoiceViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `opens idle`() = runTest {
        assertEquals(GroupChoiceState(), viewModel(registeredUserRepository()).state.value)
    }

    @Test
    fun `says it is working while it creates the group`() = runTest {
        val viewModel = viewModel(registeredUserRepository())

        viewModel.createGroup()

        assertEquals(GroupChoiceState(submitting = true), viewModel.state.value)
    }

    @Test
    fun `announces the group once`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        val creations = collectCreations(viewModel)

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(1, creations.size)
    }

    @Test
    fun `stops working once the group exists`() = runTest {
        val viewModel = viewModel(registeredUserRepository())

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(GroupChoiceState(), viewModel.state.value)
    }

    @Test
    fun `carries the refusal to found a second group`() = runTest {
        val viewModel = viewModel(registeredUserRepository(), groupRepositoryWithAGroup())

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(
            GroupChoiceState(error = CreateGroupError.GroupAlreadyExists),
            viewModel.state.value,
        )
    }

    @Test
    fun `carries the refusal when the registration was lost`() = runTest {
        val viewModel = viewModel(AmnesicUserRepository())

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(
            GroupChoiceState(error = CreateGroupError.NotRegistered),
            viewModel.state.value,
        )
    }

    @Test
    fun `creates one group however many times the button is pressed`() = runTest {
        val groups = RamGroupRepository()
        val viewModel = viewModel(registeredUserRepository(), groups)
        val creations = collectCreations(viewModel)

        viewModel.createGroup()
        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(1, creations.size)
    }

    private fun TestScope.collectCreations(viewModel: GroupChoiceViewModel): List<Unit> {
        val creations = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.groupCreated.collect { creations += it }
        }

        return creations
    }

    private fun viewModel(
        userRepository: UserRepository,
        groupRepository: GroupRepository = RamGroupRepository(),
    ) = GroupChoiceViewModel(
        Onboarding(userRepository, groupRepository, SampleInvitationRepository()),
    )
}
