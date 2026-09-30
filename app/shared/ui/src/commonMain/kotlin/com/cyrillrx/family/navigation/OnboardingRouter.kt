package com.cyrillrx.family.navigation

import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.app.MainRoute

interface OnboardingRouter {
    fun openHome()
}

class OnboardingRouterImpl(private val backStack: NavBackStack<NavKey>) : OnboardingRouter {

    // One mutation: `NavDisplay` requires a back stack that is never empty, and the two writes
    // leave it empty in between.
    override fun openHome() {
        Snapshot.withMutableSnapshot {
            backStack.clear()
            backStack.add(MainRoute.Home)
        }
    }
}
