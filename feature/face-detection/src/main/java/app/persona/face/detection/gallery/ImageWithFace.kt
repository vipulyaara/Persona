package app.persona.face.detection.gallery

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.persona.data.detection.FaceDetection

@Composable
fun ImageWithFace(
    bitmap: Bitmap,
    aspectRatio: Float,
    detections: List<FaceDetection>?,
    onFaceNameUpdated: (FaceDetection, String) -> Unit
) {
    var selectedFace by remember { mutableStateOf<FaceDetection?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(8.dp))
    ) {
        FaceOverlay(
            modifier = Modifier.fillMaxSize(),
            detections = detections,
            imageWidth = bitmap.width,
            imageHeight = bitmap.height,
            onFaceClicked = { face -> selectedFace = face }
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Image with face",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Show dialog when a face is selected
        selectedFace?.let { face ->
            FaceNameDialog(
                currentName = face.name,
                onDismiss = { selectedFace = null },
                onConfirm = { newName ->
                    onFaceNameUpdated(face, newName)
                    selectedFace = null
                }
            )
        }
    }
}
