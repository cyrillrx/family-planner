package com.cyrillrx.family.presentation.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cyrillrx.family.presentation.component.OnboardingErrorText
import com.cyrillrx.family.presentation.component.OnboardingStep
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.onboarding_join_back
import familyplanner.shared.ui.generated.resources.onboarding_join_label
import familyplanner.shared.ui.generated.resources.onboarding_join_submit
import familyplanner.shared.ui.generated.resources.onboarding_join_subtitle
import familyplanner.shared.ui.generated.resources.onboarding_join_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun JoinGroupScreen(
    state: OnboardingState.Join,
    onCodeChanged: (String) -> Unit,
    onJoinClicked: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStep(
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

        if (state.error != null) OnboardingErrorText(state.error)

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
        state = OnboardingState.Join(code = "accepted-invitation-01"),
        onCodeChanged = {},
        onJoinClicked = {},
        onBackClicked = {},
    )
}

@Preview
@Composable
private fun JoinGroupScreenRefusedPreview() {
    JoinGroupScreen(
        state = OnboardingState.Join(code = "short", error = OnboardingError.CodeTooShort),
        onCodeChanged = {},
        onJoinClicked = {},
        onBackClicked = {},
    )
}
