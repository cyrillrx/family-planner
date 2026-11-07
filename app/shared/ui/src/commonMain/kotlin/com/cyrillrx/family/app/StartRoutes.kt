package com.cyrillrx.family.app

import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.group.domain.OnboardingStep
import com.cyrillrx.family.navigation.OnboardingRoute
import com.cyrillrx.family.start.domain.StartDestination

internal fun StartDestination.toBackStack(): List<NavKey> = when (this) {
    StartDestination.Home -> listOf(MainRoute.Home)
    is StartDestination.Onboarding -> step.toBackStack()
}

private fun OnboardingStep.toBackStack(): List<NavKey> = when (this) {
    OnboardingStep.DISPLAY_NAME -> listOf(OnboardingRoute.DisplayName)
    OnboardingStep.GROUP_CHOICE -> listOf(OnboardingRoute.DisplayName, OnboardingRoute.GroupChoice)
}
