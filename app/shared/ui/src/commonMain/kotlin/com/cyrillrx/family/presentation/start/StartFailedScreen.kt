package com.cyrillrx.family.presentation.start

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cyrillrx.family.group.domain.NoAnswer
import com.cyrillrx.family.group.domain.StoreUnavailable
import com.cyrillrx.family.presentation.component.MessageLayout
import com.cyrillrx.family.presentation.component.message
import familyplanner.app.shared.ui.generated.resources.Res
import familyplanner.app.shared.ui.generated.resources.action_retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun StartFailedScreen(error: NoAnswer, onRetryClicked: () -> Unit, modifier: Modifier = Modifier) {
    MessageLayout(message = error.message(), modifier = modifier) {
        Button(onClick = onRetryClicked, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.action_retry))
        }
    }
}

@Preview
@Composable
private fun StartFailedScreenPreview() = MaterialTheme {
    StartFailedScreen(error = StoreUnavailable(IllegalStateException("preview")), onRetryClicked = {})
}
