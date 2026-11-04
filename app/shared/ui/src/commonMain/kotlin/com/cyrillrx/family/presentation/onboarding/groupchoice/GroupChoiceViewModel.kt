package com.cyrillrx.family.presentation.onboarding.groupchoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.CreateGroupError
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

    private val groupsReady = Channel<Unit>(Channel.BUFFERED)
    val groupReady: Flow<Unit> = groupsReady.receiveAsFlow()

    fun createGroup() {
        val beforeSubmit = state.value
        if (beforeSubmit.submitting) return

        state.value = beforeSubmit.copy(submitting = true, error = null)

        viewModelScope.launch {
            when (val creation = onboarding.createGroup()) {
                is Result.Success -> groupsReady.send(Unit)

                is Result.Failure -> refuse(beforeSubmit, creation.error)
            }
        }
    }

    private suspend fun refuse(beforeSubmit: GroupChoiceState, error: CreateGroupError) {
        if (error == CreateGroupError.GroupAlreadyExists) {
            groupsReady.send(Unit)
            return
        }

        state.value = beforeSubmit.copy(error = error)
    }
}
