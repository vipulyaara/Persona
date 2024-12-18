package app.persona.domain

import android.graphics.Bitmap
import app.persona.data.detection.FaceDetection
import app.persona.data.detection.FaceDetectorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for detecting faces in images using MediaPipe ML model.
 * Follows clean architecture principles by encapsulating the face detection business logic
 * and providing a clean API for the presentation layer.
 */
@Singleton
class DetectFacesUseCase @Inject constructor(
    private val faceDetectorHelper: FaceDetectorHelper
) {
    /**
     * Executes face detection on the provided bitmap.
     * Handles the complexity of ML model interaction and provides a clean result interface.
     *
     * @param bitmap The image to process for face detection
     * @return [FaceDetectionResult] containing detection information or empty result if no faces found
     */
    suspend operator fun invoke(bitmap: Bitmap): FaceDetectionResult = withContext(Dispatchers.IO) {
        val detections = faceDetectorHelper.detectImage(bitmap)

        FaceDetectionResult(
            faceCount = detections?.size ?: 0,
            detections = detections
        )
    }

    /**
     * Cleans up resources used by the face detector.
     * Should be called when the use case is no longer needed.
     */
    fun cleanup() {
        faceDetectorHelper.clearFaceDetector()
    }
}

/**
 * Value object representing the result of face detection.
 * Immutable data structure that encapsulates all necessary detection information.
 */
data class FaceDetectionResult(
    val faceCount: Int,
    val detections: List<FaceDetection>?
)
