package com.cyrillrx.family.presentation.home

import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.model.Group
import com.cyrillrx.family.group.domain.model.GroupId
import com.cyrillrx.family.presentation.CountingGroupRepository
import com.cyrillrx.family.presentation.NOW
import com.cyrillrx.family.presentation.groupRepositoryWithAGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `is loading before it has read anything`() = runTest {
        assertEquals(HomeState(), HomeViewModel(RamGroupRepository()).state.value)
    }

    @Test
    fun `names the group it finds`() = runTest {
        val viewModel = HomeViewModel(groupRepositoryWithAGroup())

        advanceUntilIdle()

        assertEquals(HomeState(HomeState.Body.InGroup("Home")), viewModel.state.value)
    }

    @Test
    fun `waits when no group has arrived`() = runTest {
        val viewModel = HomeViewModel(RamGroupRepository())

        advanceUntilIdle()

        assertEquals(HomeState(HomeState.Body.WaitingForTheGroup), viewModel.state.value)
    }

    @Test
    fun `sees a group that arrives after the first read`() = runTest {
        val groups = RamGroupRepository()
        val viewModel = HomeViewModel(groups)
        advanceUntilIdle()
        groups.setGroup(Group(id = GroupId("group-1"), name = "Family", createdAt = NOW))

        viewModel.silentRefresh()
        advanceUntilIdle()

        assertEquals(HomeState(HomeState.Body.InGroup("Family")), viewModel.state.value)
    }

    @Test
    fun `reads again on a silent refresh`() = runTest {
        val groups = CountingGroupRepository()
        val viewModel = HomeViewModel(groups)
        advanceUntilIdle()

        viewModel.silentRefresh()
        advanceUntilIdle()

        assertEquals(2, groups.reads)
    }

    @Test
    fun `ignores a silent refresh while it is still reading`() = runTest {
        val groups = CountingGroupRepository()
        val viewModel = HomeViewModel(groups)

        viewModel.silentRefresh()
        advanceUntilIdle()

        assertEquals(1, groups.reads)
    }
}
