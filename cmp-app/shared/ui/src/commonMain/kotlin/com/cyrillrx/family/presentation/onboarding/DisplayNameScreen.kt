package com.cyrillrx.family.presentation.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cyrillrx.family.presentation.component.OnboardingErrorText
import com.cyrillrx.family.presentation.component.OnboardingStep
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.onboarding_name_continue
import familyplanner.shared.ui.generated.resources.onboarding_name_label
import familyplanner.shared.ui.generated.resources.onboarding_name_subtitle
import familyplanner.shared.ui.generated.resources.onboarding_name_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun DisplayNameScreen(
    state: OnboardingState.Name,
    onDisplayNameChanged: (String) -> Unit,
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStep(
        title = stringResource(Res.string.onboarding_name_title),
        subtitle = stringResource(Res.string.onboarding_name_subtitle),
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = state.displayName,
            onValueChange = onDisplayNameChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
            label = { Text(stringResource(Res.string.onboarding_name_label)) },
            singleLine = true,
            isError = state.error != null,
        )

        if (state.error != null) OnboardingErrorText(state.error)

        Button(
            onClick = onContinueClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
        ) {
            Text(stringResource(Res.string.onboarding_name_continue))
        }
    }
}

@Preview
@Composable
private fun DisplayNameScreenPreview() {
    DisplayNameScreen(
        state = OnboardingState.Name(displayName = "Alice"),
        onDisplayNameChanged = {},
        onContinueClicked = {},
    )
}

@Preview
@Composable
private fun DisplayNameScreenRefusedPreview() {
    DisplayNameScreen(
        state = OnboardingState.Name(error = OnboardingError.BlankDisplayName),
        onDisplayNameChanged = {},
        onContinueClicked = {},
    )
}
