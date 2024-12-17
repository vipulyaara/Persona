@file:OptIn(ExperimentalPermissionsApi::class)

package app.persona.face.detection

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import app.persona.components.MessageBox
import app.persona.theme.Dimens
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@Composable
fun PermissionHandler(
    onPermissionGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    when {
        // Regular permission granted
        permissionState.status is PermissionStatus.Granted -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                onPermissionGranted()
            }
        }

        // Permission denied cases
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.Gutter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val textToShow = if ((permissionState.status as? PermissionStatus.Denied)?.shouldShowRationale == true) {
                    "Persona needs access to your photos to detect faces in them."
                } else {
                    "Photo access permission required. Please enable it in Settings."
                }
                MessageBox(text = textToShow)
                Spacer(modifier = Modifier.height(Dimens.Spacing08))
                Button(
                    onClick = {
                        if ((permissionState.status as? PermissionStatus.Denied)?.shouldShowRationale == true) {
                            permissionState.launchPermissionRequest()
                        } else {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if ((permissionState.status as? PermissionStatus.Denied)?.shouldShowRationale == true) {
                            "Request Permission"
                        } else {
                            "Open Settings"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
                    )
                }
            }
        }
    }
}
