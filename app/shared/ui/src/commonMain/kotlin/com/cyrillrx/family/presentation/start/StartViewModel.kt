package com.cyrillrx.family.presentation.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.cyrillrx.family.app.resetUnlessReachableFrom
import com.cyrillrx.family.start.domain.AppStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StartViewModel(private val appStart: AppStart) : ViewModel() {

    val state: StateFlow<StartState>
        field = MutableStateFlow<StartState>(StartState.Resolving)

    private var backStackReconciled = false

    init {
        viewModelScope.launch {
            state.value = StartState.Resolved(appStart.destination())
        }
    }

    /**
     * Resets [backStack] unless the resolved destination can reach it, once per view model: a
     * view model that outlived its window kept a destination the member may since have left.
     */
    fun reconcile(backStack: MutableList<NavKey>) {
        val resolved = state.value as? StartState.Resolved ?: return
        if (backStackReconciled) return

        backStackReconciled = true
        backStack.resetUnlessReachableFrom(resolved.destination)
    }
}
