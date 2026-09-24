package com.cyrillrx.family.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.CreateGroupError
import com.cyrillrx.family.group.domain.JoinGroupError
import com.cyrillrx.family.group.domain.Onboarding
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RegisterError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val onboarding: Onboarding) : ViewModel() {

    val state: StateFlow<OnboardingState>
        field = MutableStateFlow<OnboardingState>(OnboardingState.Name())

    fun changeDisplayName(displayName: String) {
        val current = state.value as? OnboardingState.Name ?: return
        if (current.submitting) return

        state.value = current.copy(displayName = displayName, error = null)
    }

    fun submitDisplayName() {
        val current = state.value as? OnboardingState.Name ?: return
        if (current.submitting) return

        state.value = current.copy(submitting = true, error = null)

        viewModelScope.launch {
            state.value = when (val registered = onboarding.register(current.displayName)) {
                is Result.Success -> OnboardingState.Choice()
                is Result.Failure -> current.copy(
                    submitting = false,
                    error = registered.error.toOnboardingError(),
                )
            }
        }
    }

    fun createGroup() {
        val current = state.value as? OnboardingState.Choice ?: return
        if (current.submitting) return

        state.value = current.copy(submitting = true, error = null)

        viewModelScope.launch {
            state.value = when (val created = onboarding.createGroup()) {
                is Result.Success -> OnboardingState.Done(created.value.name)
                is Result.Failure -> current.copy(
                    submitting = false,
                    error = created.error.toOnboardingError(),
                )
            }
        }
    }

    fun openJoinGroup() {
        val current = state.value as? OnboardingState.Choice ?: return
        if (current.submitting) return

        state.value = OnboardingState.Join()
    }

    fun backToChoice() {
        val current = state.value as? OnboardingState.Join ?: return
        if (current.submitting) return

        state.value = OnboardingState.Choice()
    }

    fun changeInvitationCode(code: String) {
        val current = state.value as? OnboardingState.Join ?: return
        if (current.submitting) return

        state.value = current.copy(code = code, error = null)
    }

    fun submitInvitationCode() {
        val current = state.value as? OnboardingState.Join ?: return
        if (current.submitting) return

        state.value = current.copy(submitting = true, error = null)

        viewModelScope.launch {
            state.value = when (val joined = onboarding.joinGroup(current.code)) {
                is Result.Success -> OnboardingState.Done()
                is Result.Failure -> current.copy(
                    submitting = false,
                    error = joined.error.toOnboardingError(),
                )
            }
        }
    }
}

private fun RegisterError.toOnboardingError(): OnboardingError = when (this) {
    RegisterError.BlankDisplayName -> OnboardingError.BlankDisplayName
    is RegisterError.Registration -> OnboardingError.Unexpected
}

private fun CreateGroupError.toOnboardingError(): OnboardingError = when (this) {
    CreateGroupError.NotRegistered -> OnboardingError.Unexpected
    CreateGroupError.GroupAlreadyExists -> OnboardingError.AlreadyInAGroup
}

private fun JoinGroupError.toOnboardingError(): OnboardingError = when (this) {
    JoinGroupError.NotRegistered -> OnboardingError.Unexpected
    JoinGroupError.CodeTooShort -> OnboardingError.CodeTooShort
    JoinGroupError.AlreadyInAGroup -> OnboardingError.AlreadyInAGroup
    is JoinGroupError.Redemption -> cause.toOnboardingError()
}

private fun RedeemInvitationError.toOnboardingError(): OnboardingError = when (this) {
    RedeemInvitationError.Revoked -> OnboardingError.CodeRevoked
    RedeemInvitationError.AlreadyRedeemed -> OnboardingError.CodeAlreadyRedeemed
    RedeemInvitationError.Expired -> OnboardingError.CodeExpired
    RedeemInvitationError.Unknown,
    RedeemInvitationError.EmptyResponse,
    is RedeemInvitationError.IncompleteResponse,
    -> OnboardingError.Unexpected
}
