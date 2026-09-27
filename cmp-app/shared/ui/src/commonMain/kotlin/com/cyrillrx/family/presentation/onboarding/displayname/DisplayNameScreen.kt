package com.cyrillrx.family.presentation.onboarding.displayname

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cyrillrx.family.group.domain.RegisterError
import com.cyrillrx.family.navigation.OnboardingRouter
import com.cyrillrx.family.presentation.component.ErrorText
import com.cyrillrx.family.presentation.component.OnboardingStepLayout
import com.cyrillrx.family.presentation.component.message
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.onboarding_name_continue
import familyplanner.shared.ui.generated.resources.onboarding_name_label
import familyplanner.shared.ui.generated.resources.onboarding_name_subtitle
import familyplanner.shared.ui.generated.resources.onboarding_name_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun DisplayNameScreen(
    viewModel: DisplayNameViewModel,
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel) { viewModel.registered.collect { router.openGroupChoice() } }

    DisplayNameScreen(
        state = viewModel.state.collectAsStateWithLifecycle().value,
        onDisplayNameChanged = viewModel::changeDisplayName,
        onContinueClicked = viewModel::register,
        modifier = modifier,
    )
}

@Composable
fun DisplayNameScreen(
    state: DisplayNameState,
    onDisplayNameChanged: (String) -> Unit,
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingStepLayout(
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

        state.error?.let { ErrorText(it.message()) }

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
        state = DisplayNameState(displayName = "Alice"),
        onDisplayNameChanged = {},
        onContinueClicked = {},
    )
}

@Preview
@Composable
private fun DisplayNameScreenRefusedPreview() {
    DisplayNameScreen(
        state = DisplayNameState(error = RegisterError.BlankDisplayName),
        onDisplayNameChanged = {},
        onContinueClicked = {},
    )
}
