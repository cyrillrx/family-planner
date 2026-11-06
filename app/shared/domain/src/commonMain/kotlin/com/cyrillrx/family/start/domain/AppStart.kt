package com.cyrillrx.family.start.domain

import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.OnboardingStep

class AppStart(
    private val groupRepository: GroupRepository,
    private val onboarding: Onboarding,
) {

    suspend fun destination(): StartDestination {
        if (groupRepository.group() != null) return StartDestination.Home

        return StartDestination.Onboarding(onboarding.currentStep())
    }
}

sealed interface StartDestination {
    data object Home : StartDestination
    data class Onboarding(val step: OnboardingStep) : StartDestination
}
