package com.cyrillrx.family.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.app.MainRoute

interface OnboardingRouter {
    fun openGroupChoice()

    fun openJoinGroup()

    fun openHome()

    fun navigateUp()
}

class OnboardingRouterImpl(private val backStack: NavBackStack<NavKey>) : OnboardingRouter {

    // A second tap during the transition must not push the step twice: a repeated key
    // crashes `NavDisplay`.
    override fun openGroupChoice() {
        if (backStack.lastOrNull() == OnboardingRoute.GroupChoice) return

        backStack.add(OnboardingRoute.GroupChoice)
    }

    override fun openJoinGroup() {
        if (backStack.lastOrNull() == OnboardingRoute.JoinGroup) return

        backStack.add(OnboardingRoute.JoinGroup)
    }

    override fun openHome() {
        backStack.replaceAllWith(MainRoute.Home)
    }

    override fun navigateUp() {
        backStack.navigateUp()
    }
}
