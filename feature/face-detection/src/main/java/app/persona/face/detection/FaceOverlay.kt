package app.persona.face.detection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.persona.data.detection.FaceDetection

/**
 * A composable that draws face detection overlays on top of an image.
 * Scales the detection coordinates to match the displayed image size while
 * maintaining the correct aspect ratio.
 *
 * @param detections List of face detections containing bounding box coordinates
 * @param imageWidth Original width of the processed image
 * @param imageHeight Original height of the processed image
 * @param content The composable content (typically an Image) to overlay the face detection boxes on
 */
@Composable
fun FaceOverlay(
    modifier: Modifier = Modifier,
    detections: List<FaceDetection>?,
    imageWidth: Int,
    imageHeight: Int,
    content: @Composable () -> Unit
) {
    var canvasSize = IntSize.Zero

    Box(modifier = modifier.onSizeChanged { canvasSize = it }) {
        content()

        val density = LocalDensity.current

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (detections == null || canvasSize.width == 0 || canvasSize.height == 0) return@Canvas

            // Calculate scaling factors to map detection coordinates to canvas size
            val scaleX = canvasSize.width.toFloat() / imageWidth
            val scaleY = canvasSize.height.toFloat() / imageHeight

            detections.forEach { face ->
                val boundingBox = face.boundingBox

                // Transform face detection coordinates to match canvas dimensions
                val left = boundingBox.left * scaleX
                val top = boundingBox.top * scaleY
                val width = boundingBox.width() * scaleX
                val height = boundingBox.height() * scaleY

                drawRect(
                    color = Color.Green,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = with(density) { 2.dp.toPx() })
                )
            }
        }
    }
} 