package com.cyrillrx.family.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cyrillrx.family.group.domain.CreateGroupError
import com.cyrillrx.family.group.domain.RegisterError
import familyplanner.app.shared.ui.generated.resources.Res
import familyplanner.app.shared.ui.generated.resources.onboarding_error_already_in_a_group
import familyplanner.app.shared.ui.generated.resources.onboarding_error_blank_display_name
import familyplanner.app.shared.ui.generated.resources.onboarding_error_unexpected
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorText(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        modifier = modifier,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
fun RegisterError.message(): String = when (this) {
    RegisterError.BlankDisplayName -> stringResource(Res.string.onboarding_error_blank_display_name)
    is RegisterError.Registration -> stringResource(Res.string.onboarding_error_unexpected)
}

@Composable
fun CreateGroupError.message(): String = when (this) {
    CreateGroupError.GroupAlreadyExists -> stringResource(Res.string.onboarding_error_already_in_a_group)
    CreateGroupError.NotRegistered -> stringResource(Res.string.onboarding_error_unexpected)
}
