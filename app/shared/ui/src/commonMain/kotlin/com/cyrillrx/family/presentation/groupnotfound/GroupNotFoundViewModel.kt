package com.cyrillrx.family.presentation.groupnotfound

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

class GroupNotFoundViewModel(private val onboarding: Onboarding) : ViewModel() {

    val state: StateFlow<GroupNotFoundState>
        field = MutableStateFlow(GroupNotFoundState())

    // TODO(#84): announce without a Channel.
    private val departures = Channel<Unit>(Channel.BUFFERED)
    val left: Flow<Unit> = departures.receiveAsFlow()

    fun leaveGroup() {
        val beforeSubmit = state.value
        if (beforeSubmit.submitting) return

        state.value = beforeSubmit.copy(submitting = true, error = null)

        viewModelScope.launch {
            when (val departure = onboarding.leaveMissingGroup()) {
                is Result.Success -> departures.send(Unit)
                is Result.Failure -> state.value = beforeSubmit.copy(error = departure.error)
            }
        }
    }
}
