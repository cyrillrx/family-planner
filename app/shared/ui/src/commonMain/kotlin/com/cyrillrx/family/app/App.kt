package com.cyrillrx.family.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.cyrillrx.family.navigation.handleOnboardingRoutes
import com.cyrillrx.family.navigation.navigateUp
import com.cyrillrx.family.presentation.home.HomeScreen
import com.cyrillrx.family.presentation.home.HomeViewModel
import com.cyrillrx.family.presentation.start.StartState
import com.cyrillrx.family.presentation.start.StartViewModel
import com.cyrillrx.family.start.domain.StartDestination

@Composable
@Preview
fun App(graph: AppGraph = AppGraph.shared) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val startViewModel = viewModel { StartViewModel(graph.appStart) }

            when (val state = startViewModel.state.collectAsStateWithLifecycle().value) {
                StartState.Resolving -> Unit
                is StartState.Resolved -> AppNavigation(
                    graph = graph,
                    destination = state.destination,
                    reconcile = startViewModel::reconcile,
                )
            }
        }
    }
}

@Composable
private fun AppNavigation(
    graph: AppGraph,
    destination: StartDestination,
    reconcile: (MutableList<NavKey>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberAppBackStack(destination, reconcile)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.navigateUp() },
        // By default a view model is scoped to the window, outlives its entry and hands a
        // popped step back the state it was left in. The view model store needs the
        // saveable state holder alongside it to reach a SavedStateHandle.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            handleOnboardingRoutes(backStack, graph)

            entry<MainRoute.Home> {
                HomeScreen(viewModel { HomeViewModel(graph.groupRepository) })
            }
        },
    )
}

@Composable
internal fun rememberAppBackStack(
    destination: StartDestination,
    reconcile: (MutableList<NavKey>) -> Unit,
): NavBackStack<NavKey> {
    val backStack = rememberNavBackStack(navSavedStateConfig, *destination.toBackStack().toTypedArray())

    // During composition rather than in an effect: NavDisplay composes the entries in this same
    // pass, and a repeated key crashes it before any effect would get the chance to run.
    remember(backStack) { reconcile(backStack) }

    return backStack
}
