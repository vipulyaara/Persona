package app.persona.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.persona.theme.Dimens

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

@Composable
fun MessageBox(
    text: String,
    modifier: Modifier = Modifier,
    actionButton: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.secondary
        ),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Spacing24),
            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing12)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            if (actionButton != null) {
                Box(Modifier.align(Alignment.End)) {
                    actionButton()
                }
            }
        }
    }
}

@Composable
fun MessageButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
        )
    }
}
