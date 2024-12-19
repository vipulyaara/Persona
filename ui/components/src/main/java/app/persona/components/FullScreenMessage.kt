package app.persona.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import app.persona.theme.Dimens

@Composable
fun FullScreenMessage(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing12)
    ) {
        Box(Modifier.weight(0.6f)) {
            Text(
                text = text,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(Dimens.Spacing24),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .weight(0.4f)
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
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )

                Surface(shape = CircleShape) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        modifier = Modifier.padding(Dimens.Spacing08),
                        contentDescription = null
                    )
                }
            }
        }
    }
}
