package com.cyrillrx.family.presentation.start

import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.OnboardingStep
import com.cyrillrx.family.group.domain.RamGroupRepository
import com.cyrillrx.family.group.domain.RamUserRepository
import com.cyrillrx.family.group.domain.SampleInvitationRepository
import com.cyrillrx.family.navigation.OnboardingRoute
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

    private fun appStart(
        users: RamUserRepository = RamUserRepository(),
        groups: RamGroupRepository = RamGroupRepository(),
    ) = AppStart(groups, Onboarding(users, groups, SampleInvitationRepository()))

    private companion object {
        val DISPLAY_NAME = StartDestination.Onboarding(OnboardingStep.DISPLAY_NAME)
    }
}
