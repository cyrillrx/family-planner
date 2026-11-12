package com.cyrillrx.family.start.domain

import com.cyrillrx.core.domain.Result
import com.cyrillrx.core.domain.getOrElse
import com.cyrillrx.core.domain.map
import com.cyrillrx.family.group.domain.GroupNotFound
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.Membership
import com.cyrillrx.family.group.domain.NoAnswer
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.OnboardingStep

class AppStart(
    private val groupRepository: GroupRepository,
    private val onboarding: Onboarding,
) {

    suspend fun destination(): Result<StartDestination, NoAnswer> {
        val membership = groupRepository.group().getOrElse { error ->
            return when (error) {
                GroupNotFound -> Result.Success(StartDestination.GroupNotFound)
                is NoAnswer -> Result.Failure(error)
            }
        }

        return when (membership) {
            is Membership.InGroup -> Result.Success(StartDestination.Home)
            Membership.NoGroup -> onboarding.currentStep().map(StartDestination::Onboarding)
        }
    }
}

sealed interface StartDestination {
    data object Home : StartDestination
    data object GroupNotFound : StartDestination
    data class Onboarding(val step: OnboardingStep) : StartDestination
}
