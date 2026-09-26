package com.cyrillrx.family.presentation.onboarding.displayname

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.Onboarding
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DisplayNameViewModel(private val onboarding: Onboarding) : ViewModel() {

    val state: StateFlow<DisplayNameState>
        field = MutableStateFlow(DisplayNameState())

    val registered: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

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
                    state.value = beforeSubmit
                    registered.emit(Unit)
                }

                is Result.Failure -> state.value = beforeSubmit.copy(error = registration.error)
            }
        }
    }
}
