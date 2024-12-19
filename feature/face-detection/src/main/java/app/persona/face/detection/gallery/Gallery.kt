package app.persona.face.detection.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.persona.components.MessageBox
import app.persona.components.MessageButton
import app.persona.data.detection.FaceDetection
import app.persona.face.detection.FaceDetectionViewModel
import app.persona.face.detection.GalleryUiState
import app.persona.face.detection.permissions.PermissionState
import app.persona.face.detection.permissions.PhotoPermissionHandler
import app.persona.feature.face.detection.R
import app.persona.theme.Dimens

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    viewModel: FaceDetectionViewModel = hiltViewModel()
) {
    var permission by remember { mutableStateOf(PermissionState.Denied) }

    PhotoPermissionHandler(
        onPermissionGranted = { permissionState ->
            permission = permissionState
        }
    )

    LaunchedEffect(permission) {
        if (permission.hasAccess()) viewModel.scanImages(reset = true)
    }

    if (permission.hasAccess()) {
        Box(modifier = modifier) {
            GalleryContent(
                viewModel = viewModel,
                hasLimitedAccess = permission == PermissionState.Partial
            )
        }
    }
}

@Composable
private fun GalleryContent(
    viewModel: FaceDetectionViewModel,
    hasLimitedAccess: Boolean,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.padding(Dimens.Gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            GalleryUiState.Initial -> {}

            GalleryUiState.Loading -> SuccessState(
                state = GalleryUiState.Success(images = emptyList(), hasMore = true),
                onLoadMore = { viewModel.scanImages(onlyLatestSelection = hasLimitedAccess) },
                onFaceNameUpdated = viewModel::updateFaceName
            )

            is GalleryUiState.Success -> SuccessState(
                state = state,
                onLoadMore = { viewModel.scanImages(onlyLatestSelection = hasLimitedAccess) },
                onFaceNameUpdated = viewModel::updateFaceName
            )

            is GalleryUiState.Error -> ErrorState(
                error = state.error,
                onRetry = {
                    viewModel.scanImages(
                        reset = true,
                        onlyLatestSelection = hasLimitedAccess
                    )
                }
            )
        }
    }
}

@Composable
private fun SuccessState(
    state: GalleryUiState.Success,
    onLoadMore: () -> Unit,
    onFaceNameUpdated: (FaceDetection, String) -> Unit
) {
    if (state.isEmpty) {
        MessageBox(text = stringResource(R.string.no_photos_found))
    } else {
        GalleryGrid(
            images = state.images,
            hasMore = state.hasMore,
            onLoadMore = onLoadMore,
            onFaceNameUpdated = onFaceNameUpdated
        )
    }
}

@Composable
private fun ErrorState(error: Throwable, onRetry: () -> Unit) {
    MessageBox(error.localizedMessage.orEmpty()) {
        MessageButton(stringResource(R.string.retry), onClick = onRetry)
    }
}
