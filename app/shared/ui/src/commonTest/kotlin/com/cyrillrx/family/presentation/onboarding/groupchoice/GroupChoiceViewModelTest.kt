package com.cyrillrx.family.presentation.onboarding.groupchoice

import com.cyrillrx.family.group.domain.CreateGroupError
import com.cyrillrx.family.group.domain.CreateGroupWriteError
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.UserRepository
import com.cyrillrx.family.presentation.AmnesicUserRepository
import com.cyrillrx.family.presentation.RefusingGroupRepository
import com.cyrillrx.family.presentation.UnavailableUserRepository
import com.cyrillrx.family.presentation.groupRepositoryWithAGroup
import com.cyrillrx.family.presentation.registeredUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
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
        val announcements = collectAnnouncements(viewModel)

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(1, announcements.size)
    }

    @Test
    fun `announces the group to a collector that arrives after it`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        viewModel.createGroup()
        advanceUntilIdle()

        val announcements = collectAnnouncements(viewModel)
        advanceUntilIdle()

        assertEquals(1, announcements.size)
    }

    @Test
    fun `does not announce the group again to the collector that follows`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        viewModel.createGroup()
        advanceUntilIdle()
        collectAnnouncements(viewModel)
        advanceUntilIdle()

        val laterAnnouncements = collectAnnouncements(viewModel)
        advanceUntilIdle()

        assertEquals(0, laterAnnouncements.size)
    }

    @Test
    fun `keeps working once the group exists`() = runTest {
        val viewModel = viewModel(registeredUserRepository())

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(GroupChoiceState(submitting = true), viewModel.state.value)
    }

    @Test
    fun `announces the group the user already belongs to`() = runTest {
        val viewModel = viewModel(registeredUserRepository(), groupRepositoryWithAGroup())
        val announcements = collectAnnouncements(viewModel)

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(1, announcements.size)
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
    fun `carries an unavailable store rather than a lost registration`() = runTest {
        val viewModel = viewModel(UnavailableUserRepository())

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(GroupChoiceState(error = CreateGroupError.StoreUnavailable), viewModel.state.value)
    }

    @Test
    fun `carries the refusal of a store that could not write the group`() = runTest {
        val viewModel = viewModel(registeredUserRepository(), RefusingGroupRepository())

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(
            GroupChoiceState(error = CreateGroupError.Creation(CreateGroupWriteError.Unknown)),
            viewModel.state.value,
        )
    }

    @Test
    fun `does not announce a group the store could not write`() = runTest {
        val viewModel = viewModel(registeredUserRepository(), RefusingGroupRepository())
        val announcements = collectAnnouncements(viewModel)

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(0, announcements.size)
    }

    @Test
    fun `creates one group however many times the button is pressed`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        val announcements = collectAnnouncements(viewModel)

        viewModel.createGroup()
        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(1, announcements.size)
    }

    @Test
    fun `creates one group when the button is pressed again after the creation`() = runTest {
        val viewModel = viewModel(registeredUserRepository())
        val announcements = collectAnnouncements(viewModel)
        viewModel.createGroup()
        advanceUntilIdle()

        viewModel.createGroup()
        advanceUntilIdle()

        assertEquals(1, announcements.size)
    }

    private fun TestScope.collectAnnouncements(viewModel: GroupChoiceViewModel): List<Unit> {
        val announcements = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.groupReady.collect { announcements += it }
        }

        return announcements
    }

    private fun viewModel(
        userRepository: UserRepository,
        groupRepository: GroupRepository = RamGroupRepository(),
    ) = GroupChoiceViewModel(
        Onboarding(userRepository, groupRepository, SampleInvitationRepository()),
    )
}
