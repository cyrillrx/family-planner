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
import com.cyrillrx.family.navigation.OnboardingRoute
import com.cyrillrx.family.navigation.OnboardingRouterImpl
import com.cyrillrx.family.navigation.handleOnboardingRoutes
import com.cyrillrx.family.navigation.navigateUp
import com.cyrillrx.family.presentation.groupnotfound.GroupNotFoundScreen
import com.cyrillrx.family.presentation.groupnotfound.GroupNotFoundViewModel
import com.cyrillrx.family.presentation.home.HomeScreen
import com.cyrillrx.family.presentation.home.HomeViewModel
import com.cyrillrx.family.presentation.start.StartResolvingScreen
import com.cyrillrx.family.presentation.start.StartState
import com.cyrillrx.family.presentation.start.StartUnavailableScreen
import com.cyrillrx.family.presentation.start.StartViewModel

@Composable
fun App(graph: AppGraph = AppGraph.shared) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val startViewModel = viewModel { StartViewModel(graph.appStart) }
            val backStack = rememberNavBackStack(navSavedStateConfig)

            when (val state = startViewModel.state.collectAsStateWithLifecycle().value) {
                StartState.Resolving -> StartResolvingScreen()

                is StartState.Resolved -> {
                    // During composition rather than in an effect: NavDisplay composes the entries in
                    // this same pass, and a repeated key crashes it before any effect would get the
                    // chance to run.
                    remember(backStack) { startViewModel.reconcile(backStack, state.destination) }
                    AppNavigation(graph, backStack)
                }

                // TODO(#90): resolve the start again once the store is reachable, and reconcile then.
                StartState.Unavailable if backStack.canResumeWithoutStart() -> AppNavigation(graph, backStack)

                StartState.Unavailable -> StartUnavailableScreen(onRetryClicked = startViewModel::retry)
            }
        }
    }
}

@Composable
private fun AppNavigation(
    graph: AppGraph,
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    val router = remember(backStack) { OnboardingRouterImpl(backStack) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = backStack::navigateUp,
        // By default a view model is scoped to the window, outlives its entry and hands a
        // popped step back the state it was left in. The view model store needs the
        // saveable state holder alongside it to reach a SavedStateHandle.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            handleOnboardingRoutes(router, graph)

            entry<MainRoute.Home> {
                HomeScreen(
                    viewModel { HomeViewModel(graph.groupRepository) },
                    onGroupNotFound = router::openGroupNotFound,
                )
            }

            entry<MainRoute.GroupNotFound> {
                GroupNotFoundScreen(viewModel { GroupNotFoundViewModel(graph.onboarding) }, router)
            }
        },
    )
}

@Preview
@Composable
private fun AppNavigationPreview() = MaterialTheme {
    AppNavigation(
        graph = AppGraph.shared,
        backStack = rememberNavBackStack(navSavedStateConfig, OnboardingRoute.DisplayName),
    )
}
