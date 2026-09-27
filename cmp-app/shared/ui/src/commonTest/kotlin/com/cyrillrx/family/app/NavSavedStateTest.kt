package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.navigation.OnboardingRoute
import kotlin.test.Test
import kotlin.test.assertEquals

class NavSavedStateTest {

    @Test
    fun `leaves a back stack the fallback never touched`() {
        val backStack = backStackOf(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice)

        backStack.resetIfRestoredThroughFallback()

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice), backStack)
    }

    @Test
    fun `leaves a back stack that never reached the first step`() {
        val backStack = backStackOf(MainRoute.Home)

        backStack.resetIfRestoredThroughFallback()

        assertEquals(listOf<NavKey>(MainRoute.Home), backStack)
    }

    @Test
    fun `resets a back stack the fallback filled entirely`() {
        val backStack = backStackOf(UnrecognizedRoute, UnrecognizedRoute, UnrecognizedRoute)

        backStack.resetIfRestoredThroughFallback()

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName), backStack)
    }

    @Test
    fun `resets when a single route came back unrecognized`() {
        val backStack = backStackOf(OnboardingRoute.DisplayName, UnrecognizedRoute)

        backStack.resetIfRestoredThroughFallback()

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName), backStack)
    }

    @Test
    fun `resets when the unrecognized route sits under a known one`() {
        val backStack = backStackOf(UnrecognizedRoute, MainRoute.Home)

        backStack.resetIfRestoredThroughFallback()

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName), backStack)
    }

    private fun backStackOf(vararg keys: NavKey) = keys.toMutableList()
}
