package app.persona.face.detection.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.persona.components.FullScreenMessage
import app.persona.components.MessageBox
import app.persona.data.detection.FaceDetection
import app.persona.face.detection.FaceDetectionViewModel
import app.persona.face.detection.GalleryUiState
import app.persona.face.detection.gallery.overlays.LimitedAccessHeader
import app.persona.face.detection.permissions.rememberGalleryPermissionState
import app.persona.feature.face.detection.R
import app.persona.theme.Dimens

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: FaceDetectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState = rememberGalleryPermissionState(
        onPermissionGranted = { viewModel.scanImages(reset = true) }
    )

    if (permissionState.hasAccess) {
        GalleryContent(
            uiState = uiState,
            hasPartialAccess = permissionState.isPartialAccess,
            modifier = modifier,
            contentPadding = contentPadding,
            onRequestPermission = permissionState::requestPermission,
            onLoadMore = { viewModel.scanImages() },
            onRetry = { viewModel.scanImages(reset = true) },
            onFaceNameUpdated = viewModel::updateFaceName
        )
    } else {
        GalleryPermissionRequest(
            showSettings = permissionState.shouldShowSettings,
            onRequestPermission = permissionState::requestPermission,
            onOpenSettings = { openSettings(context) }
        )
    }
}

@Composable
private fun GalleryContent(
    uiState: GalleryUiState,
    hasPartialAccess: Boolean,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onFaceNameUpdated: (FaceDetection, String) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = Dimens.Gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            GalleryUiState.Initial -> Unit
            GalleryUiState.Loading -> {
                LoadingGalleryGrid(
                    contentPadding = contentPadding,
                    header = {
                        if (hasPartialAccess) {
                            LimitedAccessHeader(onRequestPermission)
                        }
                    }
                )
            }

            is GalleryUiState.Success -> {
                if (uiState.isEmpty) {
                    EmptyGalleryView(
                        contentPadding = contentPadding,
                        hasPartialAccess = hasPartialAccess,
                        onRequestPermission = onRequestPermission
                    )
                } else {
                    GalleryGrid(
                        images = uiState.images,
                        hasMore = uiState.hasMore,
                        contentPadding = contentPadding,
                        onLoadMore = onLoadMore,
                        onFaceNameUpdated = onFaceNameUpdated,
                        header = {
                            if (hasPartialAccess) {
                                LimitedAccessHeader(onRequestPermission)
                            }
                        }
                    )
                }
            }

            is GalleryUiState.Error -> ErrorMessage(
                error = uiState.error,
                contentPadding = contentPadding,
                onRetry = onRetry
            )
        }
    }
}

@Composable
private fun GalleryPermissionRequest(
    showSettings: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    FullScreenMessage(
        text = stringResource(R.string.permission_request_message),
        actionText = stringResource(
            if (showSettings) R.string.open_settings else R.string.request_permission
        )
    ) {
        if (showSettings) onOpenSettings() else onRequestPermission()
    }
}

@Composable
private fun ErrorMessage(
    error: Throwable,
    contentPadding: PaddingValues,
    onRetry: () -> Unit
) {
    MessageBox(
        text = error.localizedMessage.orEmpty(),
        modifier = Modifier.padding(contentPadding),
        actionText = stringResource(R.string.retry),
        onClick = onRetry
    )
}

private fun openSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    })
}
