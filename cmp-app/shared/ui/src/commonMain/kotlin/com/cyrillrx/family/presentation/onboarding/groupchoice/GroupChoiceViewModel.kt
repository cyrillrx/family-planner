package com.cyrillrx.family.presentation.onboarding.groupchoice

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

class GroupChoiceViewModel(private val onboarding: Onboarding) : ViewModel() {

    val state: StateFlow<GroupChoiceState>
        field = MutableStateFlow(GroupChoiceState())

    // Buffered so an emission with no collector is not lost, consumed once so coming back to
    // the step does not navigate away from it again.
    private val groupCreations = Channel<Unit>(Channel.BUFFERED)
    val groupCreated: Flow<Unit> = groupCreations.receiveAsFlow()

    fun createGroup() {
        val beforeSubmit = state.value
        if (beforeSubmit.submitting) return

        state.value = beforeSubmit.copy(submitting = true, error = null)

        viewModelScope.launch {
            when (val creation = onboarding.createGroup()) {
                is Result.Success -> {
                    state.value = beforeSubmit.copy(error = null)
                    groupCreations.send(Unit)
                }

                is Result.Failure -> state.value = beforeSubmit.copy(error = creation.error)
            }
        }
    }
}
