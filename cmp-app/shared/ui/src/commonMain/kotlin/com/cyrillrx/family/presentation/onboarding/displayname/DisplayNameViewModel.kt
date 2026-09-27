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

    // Buffered so an emission with no collector is not lost, consumed once so coming back to
    // the step does not navigate away from it again.
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
                is Result.Success -> {
                    state.value = beforeSubmit.copy(error = null)
                    registrations.send(Unit)
                }

                is Result.Failure -> state.value = beforeSubmit.copy(error = registration.error)
            }
        }
    }
}
