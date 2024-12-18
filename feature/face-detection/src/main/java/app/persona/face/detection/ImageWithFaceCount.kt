package app.persona.face.detection

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.persona.media.detection.FaceDetectorHelper
import com.google.mediapipe.tasks.components.containers.Detection
import com.google.mediapipe.tasks.vision.core.RunningMode

@Composable
fun ImageWithFaceCount(
    uri: Uri,
    faceCount: Int,
    initialDetections: List<Detection>? = null
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var detections by remember { mutableStateOf(initialDetections) }
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    val faceDetector = remember {
        FaceDetectorHelper(context = context, runningMode = RunningMode.IMAGE)
    }

    LaunchedEffect(uri) {
        try {
            val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            bitmap = bmp
            aspectRatio = bmp.width.toFloat() / bmp.height.toFloat()
            if (initialDetections == null) {
                val result = faceDetector.detectImage(bmp)
                detections = result?.results?.flatMap { it.detections() }
            }
        } catch (e: Exception) {
            bitmap = null
            detections = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(8.dp))
    ) {
        bitmap?.let { bmp ->
            FaceOverlay(
                modifier = Modifier.fillMaxSize(),
                detections = detections,
                imageWidth = bmp.width,
                imageHeight = bmp.height
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Image with $faceCount faces",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
