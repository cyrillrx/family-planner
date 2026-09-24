package com.cyrillrx.family.presentation.onboarding

/**
 * One step of the onboarding, in order. The flow is linear and has no back stack worth keeping,
 * so the step is state rather than a route — see the follow-ups of the pull request that added it.
 */
sealed interface OnboardingState {

    data class Name(
        val displayName: String = "",
        val submitting: Boolean = false,
        val error: OnboardingError? = null,
    ) : OnboardingState

    data class Choice(
        val submitting: Boolean = false,
        val error: OnboardingError? = null,
    ) : OnboardingState

    data class Join(
        val code: String = "",
        val submitting: Boolean = false,
        val error: OnboardingError? = null,
    ) : OnboardingState

    /**
     * The terminal step. [groupName] is null for a member who joined: the group is the service's
     * write and reaches this device by synchronization, so there is nothing to name yet.
     */
    data class Done(val groupName: String? = null) : OnboardingState
}

/** What went wrong, in the terms the person reading the screen can act on. */
sealed interface OnboardingError {
    data object BlankDisplayName : OnboardingError
    data object CodeTooShort : OnboardingError
    data object CodeRevoked : OnboardingError
    data object CodeAlreadyRedeemed : OnboardingError
    data object CodeExpired : OnboardingError
    data object AlreadyInAGroup : OnboardingError
    data object Unexpected : OnboardingError
}
