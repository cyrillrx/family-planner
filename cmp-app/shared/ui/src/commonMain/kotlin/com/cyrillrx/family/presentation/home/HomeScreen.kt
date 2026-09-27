package com.cyrillrx.family.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.home_in_group
import familyplanner.shared.ui.generated.resources.home_waiting_for_group
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.silentRefresh() }

    HomeScreen(state = viewModel.state.collectAsStateWithLifecycle().value, modifier = modifier)
}

@Composable
fun HomeScreen(state: HomeState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (val body = state.body) {
            HomeState.Body.Loading -> Unit

            HomeState.Body.WaitingForTheGroup -> HomeMessage(
                stringResource(Res.string.home_waiting_for_group),
            )

            is HomeState.Body.InGroup -> HomeMessage(
                stringResource(Res.string.home_in_group, body.groupName),
            )
        }
    }
}

@Composable
private fun HomeMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )
}

@Preview
@Composable
private fun HomeScreenFounderPreview() =
    HomeScreen(state = HomeState(HomeState.Body.InGroup("Family")))

@Preview
@Composable
private fun HomeScreenJoinerPreview() =
    HomeScreen(state = HomeState(HomeState.Body.WaitingForTheGroup))
