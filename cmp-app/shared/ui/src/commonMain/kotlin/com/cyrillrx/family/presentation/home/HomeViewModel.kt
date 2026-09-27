package com.cyrillrx.family.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.family.group.domain.GroupRepository
import com.cyrillrx.family.group.domain.model.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val groupRepository: GroupRepository) : ViewModel() {

    val state: StateFlow<HomeState>
        field = MutableStateFlow(HomeState())

    init {
        read()
    }

    fun silentRefresh() {
        if (state.value.body == HomeState.Body.Loading) return

        read()
    }

    private fun read() {
        viewModelScope.launch {
            state.value = HomeState(body = groupRepository.group().toBody())
        }
    }
}

private fun Group?.toBody(): HomeState.Body =
    if (this == null) HomeState.Body.WaitingForTheGroup else HomeState.Body.InGroup(name)
