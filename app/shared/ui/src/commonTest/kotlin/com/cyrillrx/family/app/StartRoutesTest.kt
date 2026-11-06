package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.group.domain.OnboardingStep
import com.cyrillrx.family.navigation.OnboardingRoute
import com.cyrillrx.family.start.domain.StartDestination
import kotlin.test.Test
import kotlin.test.assertEquals

class StartRoutesTest {

    @Test
    fun `opens the display name for someone not registered yet`() {
        assertEquals(
            listOf<NavKey>(OnboardingRoute.DisplayName),
            StartDestination.Onboarding(OnboardingStep.DISPLAY_NAME).toBackStack(),
        )
    }

    @Test
    fun `keeps the display name under the group choice so the member can go back and rename`() {
        assertEquals(
            listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice),
            StartDestination.Onboarding(OnboardingStep.GROUP_CHOICE).toBackStack(),
        )
    }

    @Test
    fun `opens home alone for a member of a group`() {
        assertEquals(listOf<NavKey>(MainRoute.Home), StartDestination.Home.toBackStack())
    }

    @Test
    fun `keeps a restored back stack the start destination can reach`() {
        val backStack = backStackOf(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice, OnboardingRoute.JoinGroup)

        backStack.resetUnlessReachableFrom(StartDestination.Onboarding(OnboardingStep.GROUP_CHOICE))

        assertEquals(
            listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice, OnboardingRoute.JoinGroup),
            backStack,
        )
    }

    @Test
    fun `keeps home for a member of a group`() {
        val backStack = backStackOf(MainRoute.Home)

        backStack.resetUnlessReachableFrom(StartDestination.Home)

        assertEquals(listOf<NavKey>(MainRoute.Home), backStack)
    }

    @Test
    fun `sends back to the display name a restored group choice nobody is registered for`() {
        val backStack = backStackOf(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice)

        backStack.resetUnlessReachableFrom(StartDestination.Onboarding(OnboardingStep.DISPLAY_NAME))

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName), backStack)
    }

    @Test
    fun `sends back to the group choice a restored home without a group`() {
        val backStack = backStackOf(MainRoute.Home)

        backStack.resetUnlessReachableFrom(StartDestination.Onboarding(OnboardingStep.GROUP_CHOICE))

        assertEquals(StartDestination.Onboarding(OnboardingStep.GROUP_CHOICE).toBackStack(), backStack)
    }

    @Test
    fun `sends a member of a group past a restored onboarding`() {
        val backStack = backStackOf(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice)

        backStack.resetUnlessReachableFrom(StartDestination.Home)

        assertEquals(listOf<NavKey>(MainRoute.Home), backStack)
    }

    @Test
    fun `resets a back stack the fallback filled entirely`() {
        val backStack = backStackOf(UnrecognizedRoute, UnrecognizedRoute)

        backStack.resetUnlessReachableFrom(StartDestination.Onboarding(OnboardingStep.DISPLAY_NAME))

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName), backStack)
    }

    @Test
    fun `resets when a single route came back unrecognized`() {
        val backStack = backStackOf(OnboardingRoute.DisplayName, UnrecognizedRoute)

        backStack.resetUnlessReachableFrom(StartDestination.Onboarding(OnboardingStep.GROUP_CHOICE))

        assertEquals(StartDestination.Onboarding(OnboardingStep.GROUP_CHOICE).toBackStack(), backStack)
    }

    @Test
    fun `resets when the unrecognized route sits under a known one`() {
        val backStack = backStackOf(UnrecognizedRoute, MainRoute.Home)

        backStack.resetUnlessReachableFrom(StartDestination.Home)

        assertEquals(listOf<NavKey>(MainRoute.Home), backStack)
    }

    private fun backStackOf(vararg keys: NavKey) = keys.toMutableList()
}
