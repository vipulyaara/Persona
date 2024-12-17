package app.persona.face.detection

import android.net.Uri
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.persona.components.MessageBox
import app.persona.theme.Dimens
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
                    MessageBox("Scanning device for photos with faces...")
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

data class ProcessedImage(
    val uri: Uri,
    val faceCount: Int,
    val detections: List<Detection>? = null
)
