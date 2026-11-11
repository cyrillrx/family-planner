package com.cyrillrx.family.presentation.start

import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.OnboardingStep
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.group.domain.UserRepository
import com.cyrillrx.family.navigation.OnboardingRoute
import com.cyrillrx.family.presentation.CountingGroupRepository
import com.cyrillrx.family.presentation.IntermittentGroupRepository
import com.cyrillrx.family.presentation.groupRepositoryWithAGroup
import com.cyrillrx.family.presentation.registeredUserRepository
import com.cyrillrx.family.start.domain.AppStart
import com.cyrillrx.family.start.domain.StartDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class StartViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `is resolving before it has read anything`() = runTest {
        assertEquals(StartState.Resolving, StartViewModel(appStart()).state.value)
    }

    @Test
    fun `starts on the display name when nobody is registered`() = runTest {
        val viewModel = StartViewModel(appStart())

        advanceUntilIdle()

        assertEquals(
            StartState.Resolved(StartDestination.Onboarding(OnboardingStep.DISPLAY_NAME)),
            viewModel.state.value,
        )
    }

    @Test
    fun `starts on home for a registered member of a group`() = runTest {
        val viewModel = StartViewModel(appStart(registeredUserRepository(), groupRepositoryWithAGroup()))

        advanceUntilIdle()

        assertEquals(StartState.Resolved(StartDestination.Home), viewModel.state.value)
    }

    @Test
    fun `cannot tell where to start when the store cannot be read`() = runTest {
        val viewModel = StartViewModel(appStart(groups = IntermittentGroupRepository()))

        advanceUntilIdle()

        assertEquals(StartState.Unavailable, viewModel.state.value)
    }

    @Test
    fun `resolves again on a retry`() = runTest {
        val groups = IntermittentGroupRepository(groupRepositoryWithAGroup())
        val viewModel = StartViewModel(appStart(registeredUserRepository(), groups))
        advanceUntilIdle()
        groups.available = true

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(StartState.Resolved(StartDestination.Home), viewModel.state.value)
    }

    @Test
    fun `says it is resolving while it retries`() = runTest {
        val viewModel = StartViewModel(appStart(groups = IntermittentGroupRepository()))
        advanceUntilIdle()

        viewModel.retry()

        assertEquals(StartState.Resolving, viewModel.state.value)
    }

    @Test
    fun `ignores a retry once resolved`() = runTest {
        val groups = CountingGroupRepository()
        val viewModel = StartViewModel(appStart(groups = groups))
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(1, groups.reads)
    }

    @Test
    fun `resets a back stack the destination cannot reach`() {
        val backStack = mutableListOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice)

        StartViewModel(appStart()).reconcile(backStack, DISPLAY_NAME)

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName), backStack)
    }

    @Test
    fun `leaves alone a back stack the member advanced once it was reconciled`() {
        val viewModel = StartViewModel(appStart())
        viewModel.reconcile(mutableListOf(OnboardingRoute.DisplayName), DISPLAY_NAME)
        val advanced = mutableListOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice)

        viewModel.reconcile(advanced, DISPLAY_NAME)

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice), advanced)
    }

    @Test
    fun `fills an empty back stack handed over once it was reconciled`() {
        val viewModel = StartViewModel(appStart())
        viewModel.reconcile(mutableListOf(OnboardingRoute.DisplayName), DISPLAY_NAME)
        val recreated = mutableListOf<NavKey>()

        viewModel.reconcile(recreated, DISPLAY_NAME)

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName), recreated)
    }

    private fun appStart(
        users: UserRepository = RamUserRepository(),
        groups: GroupRepository = RamGroupRepository(),
    ) = AppStart(groups, Onboarding(users, groups, SampleInvitationRepository()))

    private companion object {
        val DISPLAY_NAME = StartDestination.Onboarding(OnboardingStep.DISPLAY_NAME)
    }
}
