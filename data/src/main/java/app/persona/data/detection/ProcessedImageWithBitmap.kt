package app.persona.data.detection

import android.graphics.Bitmap
import android.net.Uri

/**
 * Represents a processed image with face detection results.
 * Contains all necessary information for displaying the image and its face detection overlays.
 *
 * @property uri Source URI of the image
 * @property bitmap The processed bitmap, potentially resized for performance
 * @property faceCount Number of faces detected in the image
 * @property detections List of face detection results containing bounding boxes and confidence scores
 * @property aspectRatio Width/height ratio of the image, used for maintaining proper display proportions
 */
data class ProcessedImageWithBitmap(
    val uri: Uri,
    val bitmap: Bitmap,
    val faceCount: Int,
    val detections: List<FaceDetection>?,
    val aspectRatio: Float
) 