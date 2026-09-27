package com.cyrillrx.family.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.cyrillrx.family.navigation.handleOnboardingRoutes
import com.cyrillrx.family.navigation.navigateUp
import com.cyrillrx.family.presentation.home.HomeScreen
import com.cyrillrx.family.presentation.home.HomeViewModel

@Composable
@Preview
fun App(graph: AppGraph = AppGraph.shared) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val backStack = rememberAppBackStack()

            NavDisplay(
                backStack = backStack,
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
    }
}
