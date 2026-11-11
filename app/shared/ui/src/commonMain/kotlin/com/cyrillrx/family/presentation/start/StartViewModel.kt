package com.cyrillrx.family.presentation.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.cyrillrx.core.domain.Result
import com.cyrillrx.family.app.resetUnlessReachableFrom
import com.cyrillrx.family.start.domain.AppStart
import com.cyrillrx.family.start.domain.StartDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StartViewModel(private val appStart: AppStart) : ViewModel() {

    val state: StateFlow<StartState>
        field = MutableStateFlow<StartState>(StartState.Resolving)

    private var backStackReconciled = false

    init {
        resolve()
    }

    fun retry() {
        if (state.value == StartState.Unavailable) resolve()
    }

    /**
     * Resets [backStack] unless [destination] can reach it, once per view model and again whenever
     * it is handed over empty: a view model that outlived its window kept a destination the member
     * may since have left.
     */
    fun reconcile(backStack: MutableList<NavKey>, destination: StartDestination) {
        if (backStackReconciled && backStack.isNotEmpty()) return

        backStackReconciled = true
        backStack.resetUnlessReachableFrom(destination)
    }

    private fun resolve() {
        state.value = StartState.Resolving

        viewModelScope.launch {
            state.value = when (val destination = appStart.destination()) {
                is Result.Success -> StartState.Resolved(destination.value)
                is Result.Failure -> StartState.Unavailable
            }
        }
    }
}
