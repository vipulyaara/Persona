@file:OptIn(ExperimentalPermissionsApi::class)

package app.persona.face.detection.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.persona.components.FullScreenMessage
import app.persona.data.permissions.PhotoPermissionHelper
import app.persona.feature.face.detection.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun PhotoPermissionHandler(
    onPermissionGranted: @Composable (PhotoPermissionState) -> Unit
) {
    val context = LocalContext.current
    var permissionText by remember { mutableStateOf("") }
    var hasRequestedPermission by remember { mutableStateOf(false) }

    val permissions = remember {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                listOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
            }

            else -> {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = permissions,
        onPermissionsResult = { _ -> hasRequestedPermission = true }
    )

    LaunchedEffect(permissionsState.allPermissionsGranted, permissionsState.shouldShowRationale) {
        permissionText = when {
            PhotoPermissionHelper.hasFullAccess(context) -> "All permissions granted"
            PhotoPermissionHelper.hasPartialAccess(context) -> "Partial access granted"
            else -> "Persona requires permissions to access your photos to identify faces."
        }
    }

    when {
        PhotoPermissionHelper.hasFullAccess(context) -> {
            onPermissionGranted(PhotoPermissionState.Granted)
        }

        PhotoPermissionHelper.hasPartialAccess(context) -> {
            onPermissionGranted(PhotoPermissionState.Partial)
        }

        else -> {
            val showSettings = hasRequestedPermission &&
                    !permissionsState.allPermissionsGranted &&
                    !permissionsState.shouldShowRationale

            FullScreenMessage(
                text = permissionText,
                actionText = if (showSettings) {
                    stringResource(R.string.open_settings)
                } else {
                    stringResource(R.string.request_permission)
                }
            ) {
                if (showSettings) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
        }
    }
}

enum class PhotoPermissionState {
    Denied, Granted, Partial;

    fun hasAccess() = this == Granted || this == Partial

    fun isPartial() = this == Partial
}
