package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals

class NavSavedStateTest {

    @Test
    fun `leaves a back stack the fallback never touched`() {
        val backStack = backStackOf(MainRoute.Home)

        backStack.resetIfRestoredThroughFallback()

        assertEquals(listOf<NavKey>(MainRoute.Home), backStack)
    }

    @Test
    fun `resets a back stack the fallback filled entirely`() {
        val backStack = backStackOf(UnrecognizedRoute, UnrecognizedRoute, UnrecognizedRoute)

        backStack.resetIfRestoredThroughFallback()

        assertEquals(listOf<NavKey>(MainRoute.Home), backStack)
    }

    @Test
    fun `resets when a single route came back unrecognized`() {
        val backStack = backStackOf(MainRoute.Home, UnrecognizedRoute)

        backStack.resetIfRestoredThroughFallback()

        assertEquals(listOf<NavKey>(MainRoute.Home), backStack)
    }

    @Test
    fun `resets when the unrecognized route sits under a known one`() {
        val backStack = backStackOf(UnrecognizedRoute, MainRoute.Home)

        backStack.resetIfRestoredThroughFallback()

        assertEquals(listOf<NavKey>(MainRoute.Home), backStack)
    }

    private fun backStackOf(vararg keys: NavKey) = keys.toMutableList()
}
