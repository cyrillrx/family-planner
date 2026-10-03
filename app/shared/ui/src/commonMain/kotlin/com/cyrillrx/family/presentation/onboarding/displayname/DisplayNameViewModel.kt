package com.cyrillrx.family.presentation.onboarding.displayname

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

class DisplayNameViewModel(private val onboarding: Onboarding) : ViewModel() {

    val state: StateFlow<DisplayNameState>
        field = MutableStateFlow(DisplayNameState())

    private val registrations = Channel<Unit>(Channel.BUFFERED)
    val registered: Flow<Unit> = registrations.receiveAsFlow()

    fun changeDisplayName(displayName: String) {
        val beforeSubmit = state.value
        if (beforeSubmit.submitting) return

        state.value = beforeSubmit.copy(displayName = displayName, error = null)
    }

    fun register() {
        val beforeSubmit = state.value
        if (beforeSubmit.submitting) return

        state.value = beforeSubmit.copy(submitting = true, error = null)

        viewModelScope.launch {
            when (val registration = onboarding.register(beforeSubmit.displayName)) {
                // Ready again: the step stays on the back stack, and the member can come back to it.
                is Result.Success -> {
                    state.value = beforeSubmit
                    registrations.send(Unit)
                }

                is Result.Failure -> state.value = beforeSubmit.copy(error = registration.error)
            }
        }
    }
}
