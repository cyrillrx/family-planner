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
import familyplanner.shared.ui.generated.resources.Res
import familyplanner.shared.ui.generated.resources.home_in_group
import familyplanner.shared.ui.generated.resources.home_waiting_for_group
import org.jetbrains.compose.resources.stringResource

/** Where onboarding ends, until there is an application behind it. */
@Composable
fun HomeScreen(groupName: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (groupName == null) {
                stringResource(Res.string.home_waiting_for_group)
            } else {
                stringResource(Res.string.home_in_group, groupName)
            },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun HomeScreenFounderPreview() = HomeScreen(groupName = "Family")

@Preview
@Composable
private fun HomeScreenJoinerPreview() = HomeScreen(groupName = null)
