package com.cyrillrx.family.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.app.MainRoute
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingRouterTest {

    @Test
    fun `opens the group choice above the name step`() {
        val backStack = NavBackStack<NavKey>(OnboardingRoute.DisplayName)

        OnboardingRouterImpl(backStack).openGroupChoice()

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice), backStack.toList())
    }

    @Test
    fun `opens the group choice once when asked twice`() {
        val backStack = NavBackStack<NavKey>(OnboardingRoute.DisplayName)
        val router = OnboardingRouterImpl(backStack)

        router.openGroupChoice()
        router.openGroupChoice()

        assertEquals(listOf<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice), backStack.toList())
    }

    @Test
    fun `opens the join step above the group choice`() {
        val backStack = NavBackStack<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice)

        OnboardingRouterImpl(backStack).openJoinGroup()

        assertEquals(
            listOf<NavKey>(
                OnboardingRoute.DisplayName,
                OnboardingRoute.GroupChoice,
                OnboardingRoute.JoinGroup,
            ),
            backStack.toList(),
        )
    }

    @Test
    fun `opens the join step once when asked twice`() {
        val backStack = NavBackStack<NavKey>(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice)
        val router = OnboardingRouterImpl(backStack)

        router.openJoinGroup()
        router.openJoinGroup()

        assertEquals(
            listOf<NavKey>(
                OnboardingRoute.DisplayName,
                OnboardingRoute.GroupChoice,
                OnboardingRoute.JoinGroup,
            ),
            backStack.toList(),
        )
    }

    @Test
    fun `opens home on a back stack holding nothing else`() {
        val backStack = NavBackStack<NavKey>(
            OnboardingRoute.DisplayName,
            OnboardingRoute.GroupChoice,
            OnboardingRoute.JoinGroup,
        )

        OnboardingRouterImpl(backStack).openHome()

        assertEquals(listOf<NavKey>(MainRoute.Home), backStack.toList())
    }
}
