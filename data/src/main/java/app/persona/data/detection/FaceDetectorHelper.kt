package app.persona.data.detection

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Low-level helper class that manages MediaPipe face detector initialization and execution.
 * Handles hardware acceleration selection, model loading, and inference execution.
 * This class serves as the boundary between our application and the MediaPipe ML framework.
 */
class FaceDetectorHelper @Inject constructor(
    @ApplicationContext val context: Context,
) {
    private var threshold: Float = THRESHOLD_DEFAULT
    private var currentDelegate: Int = DELEGATE_CPU
    private var runningMode: RunningMode = RunningMode.IMAGE
    private val modelName = "face_detection_short_range.tflite"

    /**
     * MediaPipe face detector instance.
     * Implemented as a var to support runtime configuration changes and resource cleanup.
     */
    private var faceDetector: FaceDetector? = null

    init {
        setupFaceDetector()
    }

    /**
     * Releases resources used by the face detector.
     * Should be called when the detector is no longer needed or during configuration changes.
     */
    fun clearFaceDetector() {
        faceDetector?.close()
        faceDetector = null
    }

    /**
     * Initializes the face detector with current settings.
     * Thread considerations:
     * - CPU delegate can be used across threads
     * - GPU delegate must be used on the same thread that initialized it
     *
     * The initialization process includes:
     * 1. Setting up hardware acceleration options
     * 2. Configuring model parameters
     * 3. Loading the ML model
     */
    private fun setupFaceDetector() {
        val baseOptionsBuilder = BaseOptions.builder()

        // Configure hardware acceleration based on device capabilities
        when (currentDelegate) {
            DELEGATE_CPU -> {
                baseOptionsBuilder.setDelegate(Delegate.CPU)
            }

            DELEGATE_GPU -> {
                baseOptionsBuilder.setDelegate(Delegate.GPU)
            }
        }

        baseOptionsBuilder.setModelAssetPath(modelName)

        try {
            val optionsBuilder =
                FaceDetector.FaceDetectorOptions.builder()
                    .setBaseOptions(baseOptionsBuilder.build())
                    .setMinDetectionConfidence(threshold)
                    .setRunningMode(runningMode)

            val options = optionsBuilder.build()
            faceDetector = FaceDetector.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "TFLite failed to load model with error: " + e.message)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Face detector failed to load model with error: " + e.message)
        }
    }

    /**
     * Checks if the detector has been released or not properly initialized.
     */
    fun isClosed(): Boolean = faceDetector == null

    /**
     * Performs face detection on the provided bitmap.
     * The process includes:
     * 1. Converting the bitmap to MediaPipe's image format
     * 2. Running inference using the ML model
     * 3. Measuring inference time for performance monitoring
     *
     * @param image Input bitmap to process
     * @return [ResultBundle] containing detection results and performance metrics, or null if detection fails
     * @throws IllegalArgumentException if running mode is not set to IMAGE
     */
    fun detectImage(image: Bitmap): List<FaceDetection>? {
        if (runningMode != RunningMode.IMAGE) {
            throw IllegalArgumentException(
                "Attempting to call detectImage while not using RunningMode.IMAGE"
            )
        }

        if (faceDetector == null) return null

        val startTime = SystemClock.uptimeMillis()
        val mpImage = BitmapImageBuilder(image).build()

        faceDetector?.detect(mpImage)?.also { detectionResult ->
            val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
            val resultBundle = ResultBundle(
                results = listOf(detectionResult),
                inferenceTime = inferenceTimeMs,
                inputImageHeight = image.height,
                inputImageWidth = image.width
            )

            return resultBundle.results
                .flatMap { it.detections() }
                .map { FaceDetection.fromMediaPipe(it) }
        }

        return null
    }

    /**
     * Encapsulates the results of face detection inference.
     * Includes both detection results and performance metrics for analysis.
     *
     * @property results List of face detection results from the ML model
     * @property inferenceTime Time taken to perform the detection in milliseconds
     * @property inputImageHeight Original height of the processed image
     * @property inputImageWidth Original width of the processed image
     */
    data class ResultBundle(
        val results: List<FaceDetectorResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    companion object {
        /** CPU-based inference */
        const val DELEGATE_CPU = 0

        /** GPU-based inference (requires compatible hardware) */
        const val DELEGATE_GPU = 1

        /** Minimum confidence threshold for face detection (0.0 to 1.0) */
        const val THRESHOLD_DEFAULT = 0.5F

        private const val TAG = "FaceDetectorHelper"
    }
}
