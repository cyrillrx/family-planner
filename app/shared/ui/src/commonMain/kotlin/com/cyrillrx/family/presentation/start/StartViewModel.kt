package com.cyrillrx.family.presentation.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyrillrx.family.start.domain.AppStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StartViewModel(private val appStart: AppStart) : ViewModel() {

    val state: StateFlow<StartState>
        field = MutableStateFlow<StartState>(StartState.Resolving)

    init {
        viewModelScope.launch {
            state.value = StartState.Resolved(appStart.destination())
        }
    }
}
