package com.cyrillrx.family.presentation.onboarding.groupchoice

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyrillrx.family.navigation.OnboardingRouter
import com.cyrillrx.family.presentation.component.ErrorText
import com.cyrillrx.family.presentation.component.OnboardingStepLayout
import com.cyrillrx.family.presentation.component.message
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.onboarding_choice_create
import familyplanner.shared.ui.generated.resources.onboarding_choice_join
import familyplanner.shared.ui.generated.resources.onboarding_choice_subtitle
import familyplanner.shared.ui.generated.resources.onboarding_choice_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun GroupChoiceScreen(
    viewModel: GroupChoiceViewModel,
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel) { viewModel.groupCreated.collect { router.openHome() } }

    GroupChoiceScreen(
        state = viewModel.state.collectAsStateWithLifecycle().value,
        onCreateGroupClicked = viewModel::createGroup,
        onJoinGroupClicked = router::openJoinGroup,
        modifier = modifier,
    )
}

@Composable
fun GroupChoiceScreen(
    state: GroupChoiceState,
    onCreateGroupClicked: () -> Unit,
    onJoinGroupClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStepLayout(
        title = stringResource(Res.string.onboarding_choice_title),
        subtitle = stringResource(Res.string.onboarding_choice_subtitle),
        modifier = modifier,
    ) {
        state.error?.let { ErrorText(it.message()) }

        Button(
            onClick = onCreateGroupClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
        ) {
            Text(stringResource(Res.string.onboarding_choice_create))
        }

        OutlinedButton(
            onClick = onJoinGroupClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
        ) {
            Text(stringResource(Res.string.onboarding_choice_join))
        }
    }
}

@Preview
@Composable
private fun GroupChoiceScreenPreview() {
    GroupChoiceScreen(
        state = GroupChoiceState(),
        onCreateGroupClicked = {},
        onJoinGroupClicked = {},
    )
}
