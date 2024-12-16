@file:OptIn(ExperimentalPermissionsApi::class)

package app.persona.face.detection

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.persona.theme.Dimens
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mediapipe.tasks.components.containers.Detection
import kotlinx.coroutines.launch

@Composable
fun Gallery(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = viewModel { FaceDetectionViewModel(context.applicationContext) }

    val processedImages by viewModel.processedImages.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val hasMoreImages by viewModel.hasMoreImages.collectAsStateWithLifecycle()

    PermissionHandler(
        onPermissionGranted = {
            Column(
                modifier = Modifier.padding(Dimens.Gutter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isScanning && processedImages.isEmpty()) {
                    Text(
                        "Scanning device for photos with faces...",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (processedImages.isEmpty() && !isScanning) {
                    Button(onClick = {
                        scope.launch {
                            viewModel.scanImages(reset = true)
                        }
                    }) {
                        Text("Scan Device Photos")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (processedImages.isEmpty() && !isScanning) {
                    println("No faces found")
                }

                if (processedImages.isNotEmpty()) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        content = {
                            items(processedImages) { processedImage ->
                                ImageWithFaceCount(
                                    uri = processedImage.uri,
                                    faceCount = processedImage.faceCount,
                                    initialDetections = processedImage.detections
                                )
                            }

                            item(span = StaggeredGridItemSpan.FullLine) {
                                if (hasMoreImages) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isScanning) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            LaunchedEffect(Unit) {
                                                scope.launch {
                                                    viewModel.scanImages()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun PermissionHandler(
    onPermissionGranted: @Composable () -> Unit
) {
    // Update permission based on Android version
    val permissionState = rememberPermissionState(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    when (permissionState.status) {
        is PermissionStatus.Granted -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                onPermissionGranted()
            }
        }

        is PermissionStatus.Denied -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.Gutter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val textToShow = if (permissionState.status.shouldShowRationale) {
                    "The app needs access to your photos to detect faces in them."
                } else {
                    "Photo access permission is required to scan for faces in your photos. " +
                            "Please grant the permission."
                }
                Text(textToShow)
                Spacer(modifier = Modifier.height(Dimens.Spacing08))
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text(
                        text = "Request permission",
                        style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
                    )
                }
            }
        }
    }
}

data class ProcessedImage(
    val uri: Uri,
    val faceCount: Int,
    val detections: List<Detection>? = null
)
