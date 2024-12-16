package app.persona.face.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Images.Media
import androidx.lifecycle.ViewModel
import app.persona.media.detection.FaceDetectorHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class FaceDetectionViewModel(private val application: Context) : ViewModel() {
    private val faceDetector = FaceDetectorHelper(
        context = application,
        runningMode = RunningMode.IMAGE
    )

    private val _processedImages = MutableStateFlow<List<ProcessedImage>>(emptyList())
    val processedImages: StateFlow<List<ProcessedImage>> = _processedImages

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _hasMoreImages = MutableStateFlow(true)
    val hasMoreImages: StateFlow<Boolean> = _hasMoreImages

    private var currentCursor: Int = 0
    private val batchSize = 20

    suspend fun scanImages(reset: Boolean = false) {
        if (_isScanning.value) return

        if (reset) {
            _processedImages.value = emptyList()
            currentCursor = 0
            _hasMoreImages.value = true
        }

        _isScanning.value = true

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                Media._ID,
                Media.DATA,
                Media.DISPLAY_NAME
            )

            application.contentResolver.query(
                Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(Media.DISPLAY_NAME)

                // Move to the current cursor position
                if (currentCursor > 0) {
                    cursor.moveToPosition(currentCursor - 1)
                }

                var processedInBatch = 0
                while (cursor.moveToNext() && processedInBatch < batchSize) {
                    val id = cursor.getLong(idColumn)
                    val fileName = cursor.getString(nameColumn)
                    val contentUri = Uri.withAppendedPath(
                        Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )

                    println("Processing image ${currentCursor + 1}: $fileName")

                    try {
                        val bitmap = loadBitmap(contentUri)
                        val result = faceDetector.detectImage(bitmap)
                        val detections = result?.results?.flatMap { it.detections() }
                        val faceCount = detections?.size ?: 0

                        if (faceCount > 0) {
                            println("Found $faceCount faces in $fileName")
                            _processedImages.value += ProcessedImage(
                                uri = contentUri,
                                faceCount = faceCount,
                                detections = detections
                            )
                        } else {
                            println("No faces found in $fileName")
                        }
                    } catch (e: Exception) {
                        println("Failed to process $fileName: ${e.message}")
                    }
                    currentCursor++
                    processedInBatch++
                }

                // Check if we have more images
                _hasMoreImages.value = !cursor.isLast
            }
        }

        println("Finished scanning batch. Total images with faces: ${_processedImages.value.size}")
        _isScanning.value = false
    }

    private fun loadBitmap(uri: Uri): Bitmap {
        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.createSource(application.contentResolver, uri)
                .let { source ->
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
                    }
                }
        } else {
            @Suppress("DEPRECATION")
            Media.getBitmap(
                application.contentResolver,
                uri
            )
        }).copy(Bitmap.Config.ARGB_8888, true)
    }
}
