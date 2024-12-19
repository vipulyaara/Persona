package app.persona.face.detection.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import app.persona.face.detection.permissions.LimitedAccessHeader
import app.persona.face.detection.permissions.PhotoPermissionHandler
import app.persona.face.detection.permissions.PhotoPermissionState
import app.persona.feature.face.detection.R
import app.persona.theme.Dimens

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: FaceDetectionViewModel = hiltViewModel()
) {
    var permission by remember { mutableStateOf(PhotoPermissionState.Denied) }

    PhotoPermissionHandler(
        onPermissionGranted = { permissionState -> permission = permissionState }
    )

    LaunchedEffect(permission) {
        if (permission.hasAccess()) viewModel.scanImages(reset = true)
    }

    if (permission.hasAccess()) {
        GalleryContent(
            viewModel = viewModel,
            hasPartialAccess = permission.isPartial(),
            modifier = modifier,
            contentPadding = contentPadding
        )
    }
}

@Composable
private fun GalleryContent(
    viewModel: FaceDetectionViewModel,
    hasPartialAccess: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.padding(horizontal = Dimens.Gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            GalleryUiState.Initial -> {}

            GalleryUiState.Loading -> SuccessState(
                state = GalleryUiState.Success(images = emptyList(), hasMore = true),
                contentPadding = contentPadding,
                onLoadMore = { viewModel.scanImages(onlyLatestSelection = hasPartialAccess) },
                onFaceNameUpdated = viewModel::updateFaceName,
                header = {
                    if (hasPartialAccess) {
                        LimitedAccessHeader {
                            viewModel.scanImages(reset = true, onlyLatestSelection = true)
                        }
                    }
                }
            )

            is GalleryUiState.Success -> SuccessState(
                state = state,
                contentPadding = contentPadding,
                onLoadMore = { viewModel.scanImages(onlyLatestSelection = hasPartialAccess) },
                onFaceNameUpdated = viewModel::updateFaceName,
                header = {
                    if (hasPartialAccess) {
                        LimitedAccessHeader {
                            viewModel.scanImages(reset = true, onlyLatestSelection = true)
                        }
                    }
                }
            )

            is GalleryUiState.Error -> ErrorState(
                error = state.error,
                modifier = Modifier.padding(contentPadding),
                onRetry = {
                    viewModel.scanImages(
                        reset = true,
                        onlyLatestSelection = hasPartialAccess
                    )
                }
            )
        }
    }
}

@Composable
private fun SuccessState(
    state: GalleryUiState.Success,
    contentPadding: PaddingValues = PaddingValues(),
    onLoadMore: () -> Unit,
    onFaceNameUpdated: (FaceDetection, String) -> Unit,
    header: @Composable () -> Unit = {}
) {
    if (state.isEmpty) {
        MessageBox(text = stringResource(R.string.no_photos_found))
    } else {
        GalleryGrid(
            images = state.images,
            contentPadding = contentPadding,
            hasMore = state.hasMore,
            onLoadMore = onLoadMore,
            onFaceNameUpdated = onFaceNameUpdated,
            header = header
        )
    }
}

@Composable
private fun ErrorState(error: Throwable, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    MessageBox(text = error.localizedMessage.orEmpty(), modifier = modifier) {
        MessageButton(stringResource(R.string.retry), onClick = onRetry)
    }
}
