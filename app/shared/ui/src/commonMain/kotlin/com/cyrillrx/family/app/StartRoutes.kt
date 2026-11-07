package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.group.domain.OnboardingStep
import com.cyrillrx.family.navigation.OnboardingRoute
import com.cyrillrx.family.start.domain.StartDestination

internal fun StartDestination.toBackStack(): List<NavKey> = when (this) {
    StartDestination.Home -> listOf(MainRoute.Home)
    is StartDestination.Onboarding -> step.toBackStack()
}

internal fun MutableList<NavKey>.resetUnlessReachableFrom(destination: StartDestination) {
    val reachable = destination.reachableRoutes()
    if (all { it in reachable }) return

    clear()
    addAll(destination.toBackStack())
}

private fun OnboardingStep.toBackStack(): List<NavKey> = when (this) {
    OnboardingStep.DISPLAY_NAME -> listOf(OnboardingRoute.DisplayName)
    OnboardingStep.GROUP_CHOICE -> listOf(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice)
}

private fun StartDestination.reachableRoutes(): Set<NavKey> = toBackStack().toSet() + forwardRoutes()

private fun StartDestination.forwardRoutes(): Set<NavKey> = when (this) {
    StartDestination.Home -> emptySet()
    is StartDestination.Onboarding -> step.forwardRoutes()
}

private fun OnboardingStep.forwardRoutes(): Set<NavKey> = when (this) {
    OnboardingStep.DISPLAY_NAME -> emptySet()
    OnboardingStep.GROUP_CHOICE -> setOf(OnboardingRoute.JoinGroup)
}
