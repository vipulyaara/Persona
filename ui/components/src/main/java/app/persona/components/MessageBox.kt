package app.persona.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.persona.theme.AppTheme
import app.persona.theme.Dimens

/**
 * A composable that displays a message box with text and an action button.
 * The component consists of two main sections:
 * 1. A text message displayed in the upper section
 * 2. An action button with text and forward arrow in the lower section
 *
 * The message box is designed to be used for notifications, prompts, or calls-to-action
 * within the app's UI.
 */
@Composable
fun MessageBox(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing12)
    ) {
        Box(Modifier.fillMaxWidth()) {
            Text(
                text = text,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(Dimens.Spacing24),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .background(MaterialTheme.colorScheme.inverseSurface),
        ) {
            Row(
                Modifier
                    .padding(Dimens.Spacing24)
                    .align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing16)
            ) {
                Text(
                    text = actionText,
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )

                Surface(shape = CircleShape) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        modifier = Modifier
                            .size(Dimens.Spacing24)
                            .padding(Dimens.Spacing04),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun MessageBoxPreview() {
    AppTheme {
        MessageBox(text = "Sample notification to show within UI", actionText = "Proceed") { }
    }
}
