package com.cyrillrx.family.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cyrillrx.family.navigation.NavOnboardingRouter
import com.cyrillrx.family.navigation.OnboardingStep
import com.cyrillrx.family.presentation.home.HomeScreen
import com.cyrillrx.family.presentation.home.HomeViewModel
import com.cyrillrx.family.presentation.onboarding.displayname.DisplayNameScreen
import com.cyrillrx.family.presentation.onboarding.displayname.DisplayNameViewModel
import com.cyrillrx.family.presentation.onboarding.groupchoice.GroupChoiceScreen
import com.cyrillrx.family.presentation.onboarding.groupchoice.GroupChoiceViewModel
import com.cyrillrx.family.presentation.onboarding.joingroup.JoinGroupScreen
import com.cyrillrx.family.presentation.onboarding.joingroup.JoinGroupViewModel

@Composable
@Preview
fun App(graph: AppGraph = remember { AppGraph() }) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val router = remember(navController) { NavOnboardingRouter(navController) }

            NavHost(
                navController = navController,
                startDestination = OnboardingStep.DisplayName.route,
            ) {
                composable(OnboardingStep.DisplayName.route) {
                    DisplayNameScreen(viewModel { DisplayNameViewModel(graph.onboarding) }, router)
                }

                composable(OnboardingStep.GroupChoice.route) {
                    GroupChoiceScreen(viewModel { GroupChoiceViewModel(graph.onboarding) }, router)
                }

                composable(OnboardingStep.JoinGroup.route) {
                    JoinGroupScreen(viewModel { JoinGroupViewModel(graph.onboarding) }, router)
                }

                composable(OnboardingStep.Home.route) {
                    HomeScreen(viewModel { HomeViewModel(graph.groupRepository) })
                }
            }
        }
    }
}
