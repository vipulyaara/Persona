package app.persona.face.detection.gallery

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.persona.data.detection.FaceDetection

@Composable
fun ImageWithFace(
    bitmap: Bitmap,
    faceCount: Int,
    aspectRatio: Float,
    detections: List<FaceDetection>?
) {
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
            imageHeight = bitmap.height
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Image with $faceCount faces",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
