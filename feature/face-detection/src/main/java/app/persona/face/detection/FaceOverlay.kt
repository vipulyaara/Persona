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
import com.google.mediapipe.tasks.components.containers.Detection

@Composable
fun FaceOverlay(
    modifier: Modifier = Modifier,
    detections: List<Detection>?,
    imageWidth: Int,
    imageHeight: Int,
    content: @Composable () -> Unit
) {
    var canvasSize = IntSize.Zero

    Box(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
    ) {
        content()

        val density = LocalDensity.current

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (detections == null || canvasSize.width == 0 || canvasSize.height == 0) return@Canvas

            val scaleX = canvasSize.width.toFloat() / imageWidth
            val scaleY = canvasSize.height.toFloat() / imageHeight

            detections.forEach { face ->
                val boundingBox = face.boundingBox()

                // Convert coordinates to canvas space
                val left = boundingBox.left * scaleX
                val top = boundingBox.top * scaleY
                val width = boundingBox.width() * scaleX
                val height = boundingBox.height() * scaleY

                // Draw rectangle around face
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