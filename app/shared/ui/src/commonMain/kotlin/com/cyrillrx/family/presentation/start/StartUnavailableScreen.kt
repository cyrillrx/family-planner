package com.cyrillrx.family.presentation.start

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cyrillrx.family.presentation.component.MessageLayout
import familyplanner.app.shared.ui.generated.resources.Res
import familyplanner.app.shared.ui.generated.resources.action_retry
import familyplanner.app.shared.ui.generated.resources.error_store_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
fun StartUnavailableScreen(onRetryClicked: () -> Unit, modifier: Modifier = Modifier) {
    MessageLayout(message = stringResource(Res.string.error_store_unavailable), modifier = modifier) {
        Button(onClick = onRetryClicked, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.action_retry))
        }
    }
}

@Preview
@Composable
private fun StartUnavailableScreenPreview() = MaterialTheme {
    StartUnavailableScreen(onRetryClicked = {})
}
