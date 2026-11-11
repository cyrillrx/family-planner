package com.cyrillrx.family.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.group.domain.GroupNotFound
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.model.Group
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val groupRepository: GroupRepository) : ViewModel() {

    val state: StateFlow<HomeState>
        field = MutableStateFlow(HomeState())

    // TODO(#84): announce without a Channel.
    // TODO(#93): signal a missing group once for every screen.
    private val missingGroups = Channel<Unit>(Channel.BUFFERED)
    val groupNotFound: Flow<Unit> = missingGroups.receiveAsFlow()

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
                is Result.Failure if group.error == GroupNotFound -> missingGroups.send(Unit)
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
