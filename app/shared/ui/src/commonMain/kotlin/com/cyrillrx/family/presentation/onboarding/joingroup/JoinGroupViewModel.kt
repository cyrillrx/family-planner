package com.cyrillrx.family.presentation.onboarding.joingroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.Onboarding
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class JoinGroupViewModel(private val onboarding: Onboarding) : ViewModel() {

    val state: StateFlow<JoinGroupState>
        field = MutableStateFlow(JoinGroupState())

    private val joins = Channel<Unit>(Channel.BUFFERED)
    val joined: Flow<Unit> = joins.receiveAsFlow()

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
                is Result.Success -> joins.send(Unit)

                is Result.Failure -> state.value = beforeSubmit.copy(error = redemption.error)
            }
        }
    }
}
