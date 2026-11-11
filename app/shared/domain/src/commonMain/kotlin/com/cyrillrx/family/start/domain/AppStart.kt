package com.cyrillrx.family.start.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.core.domain.map
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.OnboardingStep
import com.cyrillrx.family.group.domain.StoreUnavailable

class AppStart(
    private val groupRepository: GroupRepository,
    private val onboarding: Onboarding,
) {

    suspend fun destination(): Result<StartDestination, StoreUnavailable> = when (val group = groupRepository.group()) {
        is Result.Failure -> group
        is Result.Success if group.value == null -> onboarding.currentStep().map(StartDestination::Onboarding)
        is Result.Success -> Result.Success(StartDestination.Home)
    }
}

sealed interface StartDestination {
    data object Home : StartDestination
    data class Onboarding(val step: OnboardingStep) : StartDestination
}
