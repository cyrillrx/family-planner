package com.cyrillrx.family.navigation

import androidx.compose.runtime.snapshots.Snapshot
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

    // The name step stays behind: it is the only screen that can register a member, and the
    // name it takes is the one every other member reads. A second tap during the transition must
    // not push it twice: a repeated key crashes `NavDisplay`.
    override fun openGroupChoice() {
        if (backStack.lastOrNull() == OnboardingRoute.GroupChoice) return

        backStack.add(OnboardingRoute.GroupChoice)
    }

    // One mutation: `NavDisplay` requires a back stack that is never empty, and the two writes
    // leave it empty in between.
    // Guarded like the group choice: a repeated key crashes `NavDisplay`.
    override fun openJoinGroup() {
        if (backStack.lastOrNull() == OnboardingRoute.JoinGroup) return

        backStack.add(OnboardingRoute.JoinGroup)
    }

    override fun openHome() {
        Snapshot.withMutableSnapshot {
            backStack.clear()
            backStack.add(MainRoute.Home)
        }
    }

    override fun navigateUp() {
        backStack.navigateUp()
    }
}
