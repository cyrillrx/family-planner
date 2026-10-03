package com.cyrillrx.family.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingRouterTest {

    @Test
    fun `opens the group choice above the name step`() {
        val backStack = NavBackStack<NavKey>(OnboardingRoute.DisplayName)

        OnboardingRouterImpl(backStack).openGroupChoice()

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice), backStack)
    }

    @Test
    fun `opens the group choice once when asked twice`() {
        val backStack = NavBackStack<NavKey>(OnboardingRoute.DisplayName)
        val router = OnboardingRouterImpl(backStack)

        router.openGroupChoice()
        router.openGroupChoice()

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice), backStack)
    }
}
