package com.cyrillrx.family.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cyrillrx.family.presentation.home.HomeScreen
import com.cyrillrx.family.presentation.onboarding.DisplayNameScreen
import com.cyrillrx.family.presentation.onboarding.GroupChoiceScreen
import com.cyrillrx.family.presentation.onboarding.JoinGroupScreen
import com.cyrillrx.family.presentation.onboarding.OnboardingState
import com.cyrillrx.family.presentation.onboarding.OnboardingViewModel

@Composable
@Preview
fun App(dependencies: AppDependencies = remember { AppDependencies() }) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val viewModel = viewModel { OnboardingViewModel(dependencies.onboarding) }
            val state by viewModel.state.collectAsStateWithLifecycle()

            // The step is state, not a route: the flow is linear and nothing deep-links into it.
            when (val current = state) {
                is OnboardingState.Name -> DisplayNameScreen(
                    state = current,
                    onDisplayNameChanged = viewModel::changeDisplayName,
                    onContinueClicked = viewModel::submitDisplayName,
                )

                is OnboardingState.Choice -> GroupChoiceScreen(
                    state = current,
                    onCreateGroupClicked = viewModel::createGroup,
                    onJoinGroupClicked = viewModel::openJoinGroup,
                )

                is OnboardingState.Join -> JoinGroupScreen(
                    state = current,
                    onCodeChanged = viewModel::changeInvitationCode,
                    onJoinClicked = viewModel::submitInvitationCode,
                    onBackClicked = viewModel::backToChoice,
                )

                is OnboardingState.Done -> HomeScreen(groupName = current.groupName)
            }
        }
    }
}
