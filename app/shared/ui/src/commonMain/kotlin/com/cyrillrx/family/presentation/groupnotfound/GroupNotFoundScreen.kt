package com.cyrillrx.family.presentation.groupnotfound

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.cyrillrx.family.group.domain.StoreUnavailable
import com.cyrillrx.family.navigation.OnboardingRouter
import com.cyrillrx.family.presentation.component.ErrorText
import com.cyrillrx.family.presentation.component.MessageLayout
import familyplanner.app.shared.ui.generated.resources.Res
import familyplanner.app.shared.ui.generated.resources.action_leave_group
import familyplanner.app.shared.ui.generated.resources.error_store_unavailable
import familyplanner.app.shared.ui.generated.resources.group_not_found
import org.jetbrains.compose.resources.stringResource

@Composable
fun GroupNotFoundScreen(
    viewModel: GroupNotFoundViewModel,
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel) { viewModel.left.collect { router.openGroupChoiceAfterLeavingMissingGroup() } }

    val state = viewModel.state.collectAsStateWithLifecycle().value

    // Leaving the screen clears its view model, which would cancel the departure halfway through.
    // TODO(#67): let the member go back when the departure cannot reach the store.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = state.submitting,
        onBackCompleted = {},
    )

    GroupNotFoundScreen(state = state, onLeaveClicked = viewModel::leaveGroup, modifier = modifier)
}

@Composable
fun GroupNotFoundScreen(
    state: GroupNotFoundState,
    onLeaveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MessageLayout(message = stringResource(Res.string.group_not_found), modifier = modifier) {
        if (state.error != null) ErrorText(stringResource(Res.string.error_store_unavailable))

        Button(
            onClick = onLeaveClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.submitting,
        ) {
            Text(stringResource(Res.string.action_leave_group))
        }
    }
}

@Preview
@Composable
private fun GroupNotFoundScreenPreview() = MaterialTheme {
    GroupNotFoundScreen(state = GroupNotFoundState(), onLeaveClicked = {})
}

@Preview
@Composable
private fun GroupNotFoundScreenUnavailablePreview() = MaterialTheme {
    GroupNotFoundScreen(state = GroupNotFoundState(error = StoreUnavailable), onLeaveClicked = {})
}
