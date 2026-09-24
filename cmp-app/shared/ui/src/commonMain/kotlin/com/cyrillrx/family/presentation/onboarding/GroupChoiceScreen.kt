package com.cyrillrx.family.presentation.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cyrillrx.family.presentation.component.OnboardingErrorText
import com.cyrillrx.family.presentation.component.OnboardingStep
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.onboarding_choice_create
import familyplanner.shared.ui.generated.resources.onboarding_choice_join
import familyplanner.shared.ui.generated.resources.onboarding_choice_subtitle
import familyplanner.shared.ui.generated.resources.onboarding_choice_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun GroupChoiceScreen(
    state: OnboardingState.Choice,
    onCreateGroupClicked: () -> Unit,
    onJoinGroupClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStep(
        title = stringResource(Res.string.onboarding_choice_title),
        subtitle = stringResource(Res.string.onboarding_choice_subtitle),
        modifier = modifier,
    ) {
        state.error?.let { OnboardingErrorText(it) }

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
        state = OnboardingState.Choice(),
        onCreateGroupClicked = {},
        onJoinGroupClicked = {},
    )
}
