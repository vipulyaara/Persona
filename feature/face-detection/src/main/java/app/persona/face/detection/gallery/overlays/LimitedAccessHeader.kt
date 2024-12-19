package app.persona.face.detection.gallery.overlays

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.persona.components.MessageBox
import app.persona.feature.face.detection.R
import app.persona.theme.Dimens

@Composable
fun LimitedAccessHeader(requestNewPhotos: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MessageBox(
            text = stringResource(R.string.limited_photo_access_is_granted),
            actionText = stringResource(R.string.select_more_photos),
            onClick = {
                requestNewPhotos()
            })

        Spacer(modifier = Modifier.height(Dimens.Spacing24))
    }
} 