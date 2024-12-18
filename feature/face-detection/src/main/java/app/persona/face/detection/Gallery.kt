package app.persona.face.detection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.persona.components.MessageBox
import app.persona.components.MessageButton
import app.persona.face.detection.permissions.PhotoPermissionHandler
import app.persona.feature.face.detection.R
import app.persona.theme.Dimens

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    viewModel: FaceDetectionViewModel = hiltViewModel()
) {
    PhotoPermissionHandler(
        onPermissionStateChanged = { hasAccess ->
            if (hasAccess) viewModel.scanImages(reset = true)
        },
        onPermissionGranted = { showLimitedAccess ->
            Box(modifier) {
                GalleryContent(
                    viewModel = viewModel,
                    showLimitedAccess = showLimitedAccess
                )
            }
        }
    )
}

@Composable
private fun GalleryContent(
    viewModel: FaceDetectionViewModel,
    showLimitedAccess: Boolean,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.padding(Dimens.Gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            GalleryUiState.Initial -> InitialState(
                onStartScan = {
                    viewModel.scanImages(
                        reset = true,
                        onlyLatestSelection = showLimitedAccess
                    )
                }
            )

            GalleryUiState.Loading -> SuccessState(
                state = GalleryUiState.Success(images = emptyList(), hasMore = true),
                onLoadMore = { viewModel.scanImages(onlyLatestSelection = showLimitedAccess) }
            )

            is GalleryUiState.Success -> SuccessState(
                state = state,
                onLoadMore = { viewModel.scanImages(onlyLatestSelection = showLimitedAccess) }
            )

            is GalleryUiState.Error -> ErrorState(
                error = state.error,
                onRetry = {
                    viewModel.scanImages(
                        reset = true,
                        onlyLatestSelection = showLimitedAccess
                    )
                }
            )
        }
    }
}

@Composable
private fun InitialState(onStartScan: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MessageBox(text = stringResource(R.string.gallery_tap_to_start))
        Spacer(modifier = Modifier.height(Dimens.Spacing16))
        Button(onClick = onStartScan) {
            Text(text = stringResource(R.string.gallery_start_scanning))
        }
    }
}

@Composable
private fun SuccessState(state: GalleryUiState.Success, onLoadMore: () -> Unit) {
    if (state.images.isEmpty() && !state.hasMore) {
        MessageBox(text = stringResource(R.string.gallery_no_photos_found))
    } else {
        GalleryGrid(
            images = state.images,
            hasMore = state.hasMore,
            onLoadMore = onLoadMore
        )
    }
}

@Composable
private fun ErrorState(error: Throwable, onRetry: () -> Unit) {
    MessageBox(error.localizedMessage.orEmpty()) {
        MessageButton("Retry", onClick = onRetry)
    }
}
