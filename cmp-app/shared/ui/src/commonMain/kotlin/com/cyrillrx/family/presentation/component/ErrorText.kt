package com.cyrillrx.family.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cyrillrx.family.group.domain.CreateGroupError
import com.cyrillrx.family.group.domain.JoinGroupError
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RegisterError
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.onboarding_error_already_in_a_group
import familyplanner.shared.ui.generated.resources.onboarding_error_blank_display_name
import familyplanner.shared.ui.generated.resources.onboarding_error_code_already_redeemed
import familyplanner.shared.ui.generated.resources.onboarding_error_code_expired
import familyplanner.shared.ui.generated.resources.onboarding_error_code_revoked
import familyplanner.shared.ui.generated.resources.onboarding_error_code_too_short
import familyplanner.shared.ui.generated.resources.onboarding_error_unexpected
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

@Composable
fun JoinGroupError.message(): String = when (this) {
    JoinGroupError.CodeTooShort -> stringResource(Res.string.onboarding_error_code_too_short)
    JoinGroupError.AlreadyInAGroup -> stringResource(Res.string.onboarding_error_already_in_a_group)
    JoinGroupError.NotRegistered -> stringResource(Res.string.onboarding_error_unexpected)
    is JoinGroupError.Redemption -> cause.message()
}

@Composable
fun RedeemInvitationError.message(): String = when (this) {
    RedeemInvitationError.Revoked -> stringResource(Res.string.onboarding_error_code_revoked)
    RedeemInvitationError.AlreadyRedeemed -> stringResource(Res.string.onboarding_error_code_already_redeemed)
    RedeemInvitationError.Expired -> stringResource(Res.string.onboarding_error_code_expired)
    RedeemInvitationError.Unknown,
    RedeemInvitationError.EmptyResponse,
    is RedeemInvitationError.IncompleteResponse,
    -> stringResource(Res.string.onboarding_error_unexpected)
}
