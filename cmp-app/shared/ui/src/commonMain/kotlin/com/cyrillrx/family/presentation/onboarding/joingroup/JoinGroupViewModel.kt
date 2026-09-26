package com.cyrillrx.family.presentation.onboarding.joingroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.Onboarding
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JoinGroupViewModel(private val onboarding: Onboarding) : ViewModel() {

    val state: StateFlow<JoinGroupState>
        field = MutableStateFlow(JoinGroupState())

    val joined: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun changeInvitationCode(code: String) {
        val beforeSubmit = state.value
        if (beforeSubmit.submitting) return

        state.value = beforeSubmit.copy(code = code, error = null)
    }

    fun joinGroup() {
        val beforeSubmit = state.value
        if (beforeSubmit.submitting) return

        state.value = beforeSubmit.copy(submitting = true, error = null)

        viewModelScope.launch {
            when (val redemption = onboarding.joinGroup(beforeSubmit.code)) {
                is Result.Success -> {
                    state.value = beforeSubmit
                    joined.emit(Unit)
                }

                is Result.Failure -> state.value = beforeSubmit.copy(error = redemption.error)
            }
        }
    }
}
