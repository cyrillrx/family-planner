package com.cyrillrx.family.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.cyrillrx.family.navigation.handleOnboardingRoutes
import com.cyrillrx.family.navigation.navigateUp
import com.cyrillrx.family.presentation.home.HomeScreen
import com.cyrillrx.family.presentation.home.HomeViewModel

@Composable
@Preview
fun App(graph: AppGraph = remember { AppGraph() }) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val backStack = rememberAppBackStack()

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.navigateUp() },
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
