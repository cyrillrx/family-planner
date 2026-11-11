package com.cyrillrx.family.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.model.Group
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val groupRepository: GroupRepository) : ViewModel() {

    val state: StateFlow<HomeState>
        field = MutableStateFlow(HomeState())

    private var reading: Job? = null

    init {
        read()
    }

    fun silentRefresh() {
        if (state.value.body == HomeState.Body.Loading) return

        read()
    }

    private fun read() {
        reading?.cancel()
        reading = viewModelScope.launch {
            when (val group = groupRepository.group()) {
                is Result.Success -> state.value = HomeState(body = group.value.toBody())
                is Result.Failure -> showUnavailableUnlessAnAnswerIsShown()
            }
        }
    }

    private fun showUnavailableUnlessAnAnswerIsShown() {
        if (state.value.body == HomeState.Body.Loading) state.value = HomeState(body = HomeState.Body.Unavailable)
    }
}

private fun Group?.toBody(): HomeState.Body =
    if (this == null) HomeState.Body.WaitingForTheGroup else HomeState.Body.InGroup(name)
