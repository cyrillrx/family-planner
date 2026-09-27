package com.cyrillrx.family.presentation.onboarding.groupchoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.Onboarding
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupChoiceViewModel(private val onboarding: Onboarding) : ViewModel() {

    val state: StateFlow<GroupChoiceState>
        field = MutableStateFlow(GroupChoiceState())

    val groupCreated: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun createGroup() {
        val beforeSubmit = state.value
        if (beforeSubmit.submitting) return

        state.value = beforeSubmit.copy(submitting = true, error = null)

        viewModelScope.launch {
            when (val creation = onboarding.createGroup()) {
                is Result.Success -> {
                    state.value = beforeSubmit
                    groupCreated.emit(Unit)
                }

                is Result.Failure -> state.value = beforeSubmit.copy(error = creation.error)
            }
        }
    }
}
