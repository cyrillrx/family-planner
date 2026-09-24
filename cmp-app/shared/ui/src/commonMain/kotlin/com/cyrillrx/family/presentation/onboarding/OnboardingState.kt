package com.cyrillrx.family.presentation.onboarding

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

    /** [groupName] is null until the group a member joined reaches this device. */
    data class Done(val groupName: String? = null) : OnboardingState
}

sealed interface OnboardingError {
    data object BlankDisplayName : OnboardingError
    data object CodeTooShort : OnboardingError
    data object CodeRevoked : OnboardingError
    data object CodeAlreadyRedeemed : OnboardingError
    data object CodeExpired : OnboardingError
    data object AlreadyInAGroup : OnboardingError
    data object Unexpected : OnboardingError
}
