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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import app.persona.components.MessageBox
import app.persona.theme.Dimens
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun PhotoPermissionHandler(
    onPermissionGranted: @Composable (showLimitedAccessHeader: Boolean) -> Unit,
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
            PhotoPermissionManager.hasFullAccess(context) -> "All permissions granted"
            PhotoPermissionManager.hasPartialAccess(context) -> "Partial access granted"
            else -> "Permission required to access photos"
        }

        // Notify when permission state changes
        val hasAccess = PhotoPermissionManager.hasFullAccess(context) ||
                PhotoPermissionManager.hasPartialAccess(context)
        onPermissionStateChanged(hasAccess)
    }

    when {
        PhotoPermissionManager.hasFullAccess(context) -> {
            onPermissionGranted(false)
        }

        PhotoPermissionManager.hasPartialAccess(context) -> {
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
                    RequestPermissionButton(permissionsState, showSettings, context)
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
    Button(
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
    ) {
        Text(
            text = if (!permissionsState.allPermissionsGranted) {
                "Request Permission"
            } else {
                "Open Settings"
            },
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
        )
    }
}