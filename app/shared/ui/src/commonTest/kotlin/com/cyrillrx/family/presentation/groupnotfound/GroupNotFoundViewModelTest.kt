package com.cyrillrx.family.presentation.groupnotfound

import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.StoreUnavailable
import com.cyrillrx.family.presentation.MissingGroupRepository
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
class GroupNotFoundViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `opens idle`() = runTest {
        assertEquals(GroupNotFoundState(), viewModel(MissingGroupRepository()).state.value)
    }

    @Test
    fun `says it is working while it leaves the group`() = runTest {
        val viewModel = viewModel(MissingGroupRepository())

        viewModel.leaveGroup()

        assertEquals(GroupNotFoundState(submitting = true), viewModel.state.value)
    }

    @Test
    fun `announces the departure once`() = runTest {
        val viewModel = viewModel(MissingGroupRepository())
        val departures = collectDepartures(viewModel)

        viewModel.leaveGroup()
        advanceUntilIdle()

        assertEquals(1, departures.size)
    }

    @Test
    fun `leaves once however many times the button is pressed`() = runTest {
        val groups = MissingGroupRepository()
        val viewModel = viewModel(groups)

        viewModel.leaveGroup()
        viewModel.leaveGroup()
        advanceUntilIdle()

        assertEquals(1, groups.departures)
    }

    @Test
    fun `carries an unavailable store`() = runTest {
        val viewModel = viewModel(MissingGroupRepository(reachable = false))

        viewModel.leaveGroup()
        advanceUntilIdle()

        assertEquals(GroupNotFoundState(error = StoreUnavailable), viewModel.state.value)
    }

    @Test
    fun `does not announce a departure the store could not make`() = runTest {
        val viewModel = viewModel(MissingGroupRepository(reachable = false))
        val departures = collectDepartures(viewModel)

        viewModel.leaveGroup()
        advanceUntilIdle()

        assertEquals(0, departures.size)
    }

    private fun TestScope.collectDepartures(viewModel: GroupNotFoundViewModel): List<Unit> {
        val departures = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.left.collect { departures += it }
        }

        return departures
    }

    private fun viewModel(groups: MissingGroupRepository) =
        GroupNotFoundViewModel(Onboarding(RamUserRepository(), groups, SampleInvitationRepository()))
}
