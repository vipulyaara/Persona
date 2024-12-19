@file:OptIn(ExperimentalPermissionsApi::class)

package app.persona.face.detection.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.persona.components.MessageBox
import app.persona.components.MessageButton
import app.persona.data.permissions.PhotoPermissionHelper
import app.persona.feature.face.detection.R
import app.persona.theme.Dimens
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun PhotoPermissionHandler(
    onPermissionGranted: @Composable (isLimitedAccess: Boolean) -> Unit,
    onPermissionStateChanged: (hasAccess: Boolean) -> Unit
) {
    val context = LocalContext.current
    var permissionText by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }

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

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        permissionText = when {
            PhotoPermissionHelper.hasFullAccess(context) -> "All permissions granted"
            PhotoPermissionHelper.hasPartialAccess(context) -> "Partial access granted"
            else -> "Permission required to access photos"
        }

        showSettings = !permissionsState.allPermissionsGranted
                && !permissionsState.shouldShowRationale

        // Notify when permission state changes
        val hasAccess = PhotoPermissionHelper.hasFullAccess(context) ||
                PhotoPermissionHelper.hasPartialAccess(context)
        onPermissionStateChanged(hasAccess)
    }

    when {
        PhotoPermissionHelper.hasFullAccess(context) -> {
            onPermissionGranted(false)
        }

        PhotoPermissionHelper.hasPartialAccess(context) -> {
            onPermissionGranted(true)
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.Gutter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                MessageBox(text = permissionText) {
                    RequestPermissionButton(
                        permissionsState = permissionsState,
                        showSettings = showSettings,
                        context = context
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing08))
            }
        }
    }
}

@Composable
private fun RequestPermissionButton(
    permissionsState: MultiplePermissionsState,
    showSettings: Boolean,
    context: Context
) {
    MessageButton(
        text = if (!permissionsState.allPermissionsGranted) {
            stringResource(R.string.request_permission)
        } else {
            stringResource(R.string.open_settings)
        },
        onClick = {
            if (!permissionsState.allPermissionsGranted) {
                // Always try to request permissions first
                permissionsState.launchMultiplePermissionRequest()
            } else if (showSettings) {
                // Only show settings if permissions were permanently denied
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    )
}

enum class PermissionState {
    Denied, Granted, Partial
}
