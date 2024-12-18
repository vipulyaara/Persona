package app.persona.data.detection

import android.graphics.RectF
import com.google.mediapipe.tasks.components.containers.Detection

/**
 * Domain model representing a detected face in an image.
 * Decouples the application from specific ML implementation details.
 *
 * @property boundingBox Rectangle containing the detected face
 * @property confidence Detection confidence score (0.0 to 1.0)
 * @property name Name of the detected face
 */
data class FaceDetection(
    val boundingBox: RectF,
    val confidence: Float,
    val name: String = ""
) {
    companion object {
        /**
         * Maps MediaPipe detection to our domain model.
         * Keeps ML implementation details at the boundary of our system.
         */
        fun fromMediaPipe(detection: Detection): FaceDetection = FaceDetection(
            boundingBox = detection.boundingBox(),
            confidence = detection.categories().firstOrNull()?.score() ?: 0f
        )
    }
}
