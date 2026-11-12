package com.cyrillrx.family.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.cyrillrx.family.group.domain.AlreadyInAGroup
import com.cyrillrx.family.group.domain.CreateGroupError
import com.cyrillrx.family.group.domain.GroupError
import com.cyrillrx.family.group.domain.GroupNotFound
import com.cyrillrx.family.group.domain.JoinGroupError
import com.cyrillrx.family.group.domain.NoAnswer
import com.cyrillrx.family.group.domain.RedeemInvitationError
import com.cyrillrx.family.group.domain.RegisterError
import com.cyrillrx.family.group.domain.RegisterUserError
import com.cyrillrx.family.group.domain.StoreUnavailable
import com.cyrillrx.family.group.domain.Unexpected
import familyplanner.app.shared.ui.generated.resources.Res
import familyplanner.app.shared.ui.generated.resources.error_store_unavailable
import familyplanner.app.shared.ui.generated.resources.group_not_found
import familyplanner.app.shared.ui.generated.resources.onboarding_error_blank_display_name
import familyplanner.app.shared.ui.generated.resources.onboarding_error_code_already_redeemed
import familyplanner.app.shared.ui.generated.resources.onboarding_error_code_expired
import familyplanner.app.shared.ui.generated.resources.onboarding_error_code_revoked
import familyplanner.app.shared.ui.generated.resources.onboarding_error_code_too_short
import familyplanner.app.shared.ui.generated.resources.onboarding_error_code_unknown
import familyplanner.app.shared.ui.generated.resources.onboarding_error_unexpected
import familyplanner.app.shared.ui.generated.resources.onboarding_join_error_already_in_a_group
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorText(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun RegisterError.message(): String = when (this) {
    RegisterError.BlankDisplayName -> stringResource(Res.string.onboarding_error_blank_display_name)
    is RegisterError.Registration -> cause.message()
}

@Composable
fun RegisterUserError.message(): String = when (this) {
    is NoAnswer -> message()
    RegisterUserError.Unknown,
    RegisterUserError.EmptyResponse,
    is RegisterUserError.IncompleteResponse,
    -> stringResource(Res.string.onboarding_error_unexpected)
}

@Composable
fun CreateGroupError.message(): String = when (this) {
    CreateGroupError.NotRegistered -> stringResource(Res.string.onboarding_error_unexpected)
    is CreateGroupError.Creation -> cause.message()
}

@Composable
fun JoinGroupError.message(): String = when (this) {
    JoinGroupError.CodeTooShort -> stringResource(Res.string.onboarding_error_code_too_short)
    JoinGroupError.AlreadyInAGroup -> stringResource(Res.string.onboarding_join_error_already_in_a_group)
    JoinGroupError.NotRegistered -> stringResource(Res.string.onboarding_error_unexpected)
    is JoinGroupError.Lookup -> cause.message()
    is JoinGroupError.Redemption -> cause.message()
}

@Composable
fun GroupError.message(): String = when (this) {
    GroupNotFound -> stringResource(Res.string.group_not_found)
    AlreadyInAGroup -> stringResource(Res.string.onboarding_error_unexpected)
    is NoAnswer -> message()
}

@Composable
fun NoAnswer.message(): String = when (this) {
    is StoreUnavailable -> stringResource(Res.string.error_store_unavailable)
    is Unexpected -> stringResource(Res.string.onboarding_error_unexpected)
}

@Composable
fun RedeemInvitationError.message(): String = when (this) {
    RedeemInvitationError.Revoked -> stringResource(Res.string.onboarding_error_code_revoked)
    RedeemInvitationError.AlreadyRedeemed -> stringResource(Res.string.onboarding_error_code_already_redeemed)
    RedeemInvitationError.Expired -> stringResource(Res.string.onboarding_error_code_expired)
    // The likeliest refusal of them all: a code typed by hand, or pasted one character short.
    RedeemInvitationError.Unknown -> stringResource(Res.string.onboarding_error_code_unknown)
    RedeemInvitationError.EmptyResponse,
    is RedeemInvitationError.IncompleteResponse,
    -> stringResource(Res.string.onboarding_error_unexpected)
}
