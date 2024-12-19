@file:OptIn(FlowPreview::class)

package app.persona.domain

import android.graphics.Bitmap
import android.net.Uri
import app.persona.data.detection.FaceDetection
import app.persona.data.detection.FaceDetectorHelper
import app.persona.data.detection.ProcessedImageWithBitmap
import app.persona.data.image.BitmapLoader
import app.persona.data.image.ImageBatch
import app.persona.data.image.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case responsible for detecting faces in images and processing image batches.
 * Handles the core business logic of face detection and image processing.
 */
class DetectFacesUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val bitmapLoader: BitmapLoader,
    private val faceDetectorHelper: FaceDetectorHelper
) {
    /**
     * Processes a stream of images, detecting faces in each image.
     * Images are processed in batches for efficiency.
     *
     * @param startIndex The index to start processing from
     * @param onlyLatestSelection If true, only processes recently selected images
     * @param batchSize The number of images to process in each batch
     * @return Flow of Results containing [ProcessedImageUpdate] for each processed image
     */
    operator fun invoke(
        startIndex: Int,
        onlyLatestSelection: Boolean = false,
        batchSize: Int = DEFAULT_BATCH_SIZE
    ): Flow<Result<ProcessedImageUpdate>> =
        imageRepository.getImagesStream(
            startIndex = startIndex,
            onlyLatestSelection = onlyLatestSelection,
            batchSize = batchSize
        )
            .flatMapMerge(concurrency = 2) { batch -> processBatch(batch) } //
            .catch { error -> emit(Result.failure(error)) }

    /**
     * Processes a batch of images into individual updates.
     * @param batch The batch of images to process
     * @return Flow of processed image results with batch metadata
     */
    private fun processBatch(batch: ImageBatch): Flow<Result<ProcessedImageUpdate>> = flow {
        for (imageData in batch.images) {
            bitmapLoader.loadBitmap(imageData.uri)
                .fold(
                    onSuccess = { bitmap -> processImageWithFaces(bitmap, imageData.uri) },
                    onFailure = { null }
                )?.let { processedImage ->
                    emit(Result.success(
                        ProcessedImageUpdate(
                            image = processedImage,
                            hasMore = batch.hasMore,
                            nextIndex = batch.nextIndex
                        )
                    ))
                }
        }
    }.flowOn(Dispatchers.Default)

    /**
     * Processes a single image for face detection.
     * Only returns a result if faces are detected in the image.
     *
     * @param bitmap The bitmap to analyze
     * @param uri The source URI of the image
     * @return ProcessedImageWithBitmap if faces were detected, null otherwise
     */
    private suspend fun processImageWithFaces(bitmap: Bitmap, uri: Uri): ProcessedImageWithBitmap? {
        val detectionResult = withContext(Dispatchers.IO) {
            runCatching {
                val detections = faceDetectorHelper.detectImage(bitmap)
                FaceDetectionResult(
                    faceCount = detections?.size ?: 0,
                    detections = detections?.map { detection ->
                        if (detection.name.isNotEmpty()) {
                            detection.copy(name = detection.name)
                        } else {
                            detection
                        }
                    }
                )
            }.getOrNull()
        }

        return detectionResult?.takeIf { it.faceCount > 0 }?.let { result ->
            ProcessedImageWithBitmap(
                uri = uri,
                bitmap = bitmap,
                faceCount = result.faceCount,
                detections = result.detections,
                aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            )
        }
    }

    /**
     * Cleans up resources used by the face detector.
     * Should be called when the use case is no longer needed to prevent memory leaks.
     */
    fun cleanup() {
        faceDetectorHelper.clearFaceDetector()
    }

    private companion object {
        const val DEFAULT_BATCH_SIZE = 10
    }
}

/**
 * Represents the result of face detection on a single image.
 * @property faceCount The number of faces detected
 * @property detections List of detected faces with their details
 */
data class FaceDetectionResult(
    val faceCount: Int,
    val detections: List<FaceDetection>?
)

/**
 * Represents an update from the image processing stream.
 * Contains both the processed image and batch metadata.
 *
 * @property image The processed image with face detections
 * @property hasMore Whether there are more images to process
 * @property nextIndex The index to start the next batch from
 */
data class ProcessedImageUpdate(
    val image: ProcessedImageWithBitmap,
    val hasMore: Boolean,
    val nextIndex: Int
)
