package com.cyrillrx.family.presentation.onboarding.joingroup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyrillrx.family.group.domain.JoinGroupError
import com.cyrillrx.family.navigation.OnboardingRouter
import com.cyrillrx.family.presentation.component.ErrorText
import com.cyrillrx.family.presentation.component.OnboardingStepLayout
import com.cyrillrx.family.presentation.component.message
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.onboarding_join_back
import familyplanner.shared.ui.generated.resources.onboarding_join_label
import familyplanner.shared.ui.generated.resources.onboarding_join_submit
import familyplanner.shared.ui.generated.resources.onboarding_join_subtitle
import familyplanner.shared.ui.generated.resources.onboarding_join_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun JoinGroupScreen(
    viewModel: JoinGroupViewModel,
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel) { viewModel.joined.collect { router.openHome() } }

    JoinGroupScreen(
        state = viewModel.state.collectAsStateWithLifecycle().value,
        onCodeChanged = viewModel::changeInvitationCode,
        onJoinClicked = viewModel::joinGroup,
        onBackClicked = router::navigateUp,
        modifier = modifier,
    )
}

@Composable
fun JoinGroupScreen(
    state: JoinGroupState,
    onCodeChanged: (String) -> Unit,
    onJoinClicked: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStepLayout(
        title = stringResource(Res.string.onboarding_join_title),
        subtitle = stringResource(Res.string.onboarding_join_subtitle),
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = state.code,
            onValueChange = onCodeChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
            label = { Text(stringResource(Res.string.onboarding_join_label)) },
            singleLine = true,
            isError = state.error != null,
        )

        state.error?.let { ErrorText(it.message()) }

        Button(
            onClick = onJoinClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
        ) {
            Text(stringResource(Res.string.onboarding_join_submit))
        }

        TextButton(
            onClick = onBackClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
        ) {
            Text(stringResource(Res.string.onboarding_join_back))
        }
    }
}

@Preview
@Composable
private fun JoinGroupScreenPreview() {
    JoinGroupScreen(
        state = JoinGroupState(code = "accepted-invitation-01"),
        onCodeChanged = {},
        onJoinClicked = {},
        onBackClicked = {},
    )
}

@Preview
@Composable
private fun JoinGroupScreenRefusedPreview() {
    JoinGroupScreen(
        state = JoinGroupState(code = "short", error = JoinGroupError.CodeTooShort),
        onCodeChanged = {},
        onJoinClicked = {},
        onBackClicked = {},
    )
}
