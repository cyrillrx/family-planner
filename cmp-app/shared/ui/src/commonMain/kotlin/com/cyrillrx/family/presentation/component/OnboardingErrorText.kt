package com.cyrillrx.family.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cyrillrx.family.presentation.onboarding.OnboardingError
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.onboarding_error_already_in_a_group
import familyplanner.shared.ui.generated.resources.onboarding_error_blank_display_name
import familyplanner.shared.ui.generated.resources.onboarding_error_code_already_redeemed
import familyplanner.shared.ui.generated.resources.onboarding_error_code_expired
import familyplanner.shared.ui.generated.resources.onboarding_error_code_revoked
import familyplanner.shared.ui.generated.resources.onboarding_error_code_too_short
import familyplanner.shared.ui.generated.resources.onboarding_error_unexpected
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingErrorText(error: OnboardingError, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(error.message()),
        modifier = modifier,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
    )
}

private fun OnboardingError.message(): StringResource = when (this) {
    OnboardingError.BlankDisplayName -> Res.string.onboarding_error_blank_display_name
    OnboardingError.CodeTooShort -> Res.string.onboarding_error_code_too_short
    OnboardingError.CodeRevoked -> Res.string.onboarding_error_code_revoked
    OnboardingError.CodeAlreadyRedeemed -> Res.string.onboarding_error_code_already_redeemed
    OnboardingError.CodeExpired -> Res.string.onboarding_error_code_expired
    OnboardingError.AlreadyInAGroup -> Res.string.onboarding_error_already_in_a_group
    OnboardingError.Unexpected -> Res.string.onboarding_error_unexpected
}
