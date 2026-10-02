package com.cyrillrx.family.presentation.onboarding.groupchoice

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyrillrx.family.group.domain.CreateGroupError
import com.cyrillrx.family.navigation.OnboardingRouter
import com.cyrillrx.family.presentation.component.ErrorText
import com.cyrillrx.family.presentation.component.OnboardingStepLayout
import com.cyrillrx.family.presentation.component.message
import familyplanner.app.shared.ui.generated.resources.Res
import familyplanner.app.shared.ui.generated.resources.onboarding_back
import familyplanner.app.shared.ui.generated.resources.onboarding_choice_create
import familyplanner.app.shared.ui.generated.resources.onboarding_choice_subtitle
import familyplanner.app.shared.ui.generated.resources.onboarding_choice_title
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
        onBackClicked = router::navigateUp,
        modifier = modifier,
    )
}

@Composable
fun GroupChoiceScreen(
    state: GroupChoiceState,
    onCreateGroupClicked: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStepLayout(
        title = stringResource(Res.string.onboarding_choice_title),
        subtitle = stringResource(Res.string.onboarding_choice_subtitle),
        modifier = modifier,
    ) {
        // TODO(#16): a group stranded without its founder leaves GroupAlreadyExists with no way to Home.
        state.error?.let { ErrorText(it.message()) }

        Button(
            onClick = onCreateGroupClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
        ) {
            Text(stringResource(Res.string.onboarding_choice_create))
        }

        TextButton(
            onClick = onBackClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
        ) {
            Text(stringResource(Res.string.onboarding_back))
        }
    }
}

@Preview
@Composable
private fun GroupChoiceScreenPreview() {
    GroupChoiceScreen(state = GroupChoiceState(), onCreateGroupClicked = {}, onBackClicked = {})
}

@Preview
@Composable
private fun GroupChoiceScreenRefusedPreview() {
    GroupChoiceScreen(
        state = GroupChoiceState(error = CreateGroupError.GroupAlreadyExists),
        onCreateGroupClicked = {},
        onBackClicked = {},
    )
}
