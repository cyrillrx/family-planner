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

    // The name step stays behind: it is the only screen that can register a user, and the name it
    // takes is the one every other member reads.
    override fun openGroupChoice() {
        backStack.add(OnboardingRoute.GroupChoice)
    }

    override fun openJoinGroup() {
        backStack.add(OnboardingRoute.JoinGroup)
    }

    override fun openHome() {
        backStack.clear()
        backStack.add(MainRoute.Home)
    }

    override fun navigateUp() {
        backStack.navigateUp()
    }
}
