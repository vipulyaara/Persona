package app.persona.face.detection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.persona.data.detection.FaceDetection
import app.persona.data.detection.ProcessedImageWithBitmap
import app.persona.data.image.BitmapLoader
import app.persona.data.image.ImageBatch
import app.persona.data.image.ImageRepository
import app.persona.domain.DetectFacesUseCase
import app.persona.face.detection.GalleryUiState.Error
import app.persona.face.detection.GalleryUiState.Initial
import app.persona.face.detection.GalleryUiState.Loading
import app.persona.face.detection.GalleryUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing face detection operations and UI state.
 * Handles image loading, face detection, and name management.
 * Maintains state of the scanning process and provides updates through [GalleryUiState].
 */
@HiltViewModel
class FaceDetectionViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
    private val bitmapLoader: BitmapLoader,
    private val detectFacesUseCase: DetectFacesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<GalleryUiState>(Initial)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private var currentIndex = 0
    private var isProcessing = false
    private val faceNames = mutableMapOf<String, String>() // Map to store face names

    /**
     * Updates the name of a face and propagates the change to all matching faces.
     */
    fun updateFaceName(face: FaceDetection, newName: String) {
        val key = "${face.boundingBox.left},${face.boundingBox.top},${face.boundingBox.right},${face.boundingBox.bottom}"
        faceNames[key] = newName

        _uiState.update { currentState ->
            when (currentState) {
                is Success -> {
                    // Update all matching faces with the new name
                    val updatedImages = currentState.images.map { processedImage ->
                        val updatedDetections = processedImage.detections?.map { detection ->
                            val detectionKey = "${detection.boundingBox.left},${detection.boundingBox.top},${detection.boundingBox.right},${detection.boundingBox.bottom}"
                            if (faceNames.containsKey(detectionKey)) {
                                detection.copy(name = faceNames[detectionKey] ?: "")
                            } else {
                                detection
                            }
                        }
                        processedImage.copy(detections = updatedDetections)
                    }
                    currentState.copy(images = updatedImages)
                }
                else -> currentState
            }
        }
    }

    /**
     * Initiates or continues the image scanning process.
     * @param reset If true, resets the scanning progress and starts from the beginning
     * @param onlyLatestSelection If true, only processes the most recently selected images
     */
    fun scanImages(reset: Boolean = false, onlyLatestSelection: Boolean = false) {
        if (isProcessing) return
        if (reset) resetState()

        viewModelScope.launch {
            try {
                startProcessing()
                collectAndProcessImages(onlyLatestSelection)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                isProcessing = false
            }
        }
    }

    private fun resetState() {
        currentIndex = 0
        _uiState.value = Initial
    }

    private fun startProcessing() {
        isProcessing = true
        if (_uiState.value is Initial) {
            _uiState.value = Loading
        }
    }

    /**
     * Collects and processes images in batches to prevent overwhelming the system.
     * Only keeps images where faces are detected.
     */
    private suspend fun collectAndProcessImages(onlyLatestSelection: Boolean) {
        imageRepository.getImagesStream(
            startIndex = currentIndex,
            onlyLatestSelection = onlyLatestSelection
        )
            .catch { error -> handleError(error) }
            .collect { batch -> processImageBatch(batch) }
    }

    /**
     * Processes a batch of images, detecting faces in each image.
     * Applies any existing face names to the detections.
     */
    private suspend fun processImageBatch(batch: ImageBatch) {
        batch.images.forEach { imageData ->
            bitmapLoader.loadBitmap(imageData.uri).onSuccess { bitmap ->
                detectFacesUseCase(bitmap).takeIf { it.faceCount > 0 }?.let { detectionResult ->
                    // Apply any existing face names to the detections
                    val updatedDetections = detectionResult.detections?.map { detection ->
                        val key = "${detection.boundingBox.left},${detection.boundingBox.top},${detection.boundingBox.right},${detection.boundingBox.bottom}"
                        if (faceNames.containsKey(key)) {
                            detection.copy(name = faceNames[key] ?: "")
                        } else {
                            detection
                        }
                    }

                    val processedImage = ProcessedImageWithBitmap(
                        uri = imageData.uri,
                        bitmap = bitmap,
                        faceCount = detectionResult.faceCount,
                        detections = updatedDetections,
                        aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    )
                    updateUiState(processedImage, batch.hasMore)
                }
            }
        }
        currentIndex = batch.nextIndex
    }

    private fun updateUiState(newImage: ProcessedImageWithBitmap, hasMore: Boolean) {
        _uiState.update { currentState ->
            when (currentState) {
                is Success -> currentState.copy(
                    images = currentState.images + newImage,
                    hasMore = hasMore
                )
                else -> Success(
                    images = listOf(newImage),
                    hasMore = hasMore
                )
            }
        }
    }

    private fun handleError(error: Throwable) {
        _uiState.value = GalleryUiState.Error(error)
    }

    override fun onCleared() {
        super.onCleared()
        detectFacesUseCase.cleanup()
    }
}

/**
 * Represents the different states of the gallery UI.
 * - [Initial]: Initial state before scanning starts
 * - [Loading]: Scanning is in progress
 * - [Success]: Images with faces have been found
 * - [Error]: An error occurred during scanning
 */
sealed interface GalleryUiState {
    data object Initial : GalleryUiState
    data object Loading : GalleryUiState
    data class Success(
        val images: List<ProcessedImageWithBitmap>,
        val hasMore: Boolean
    ) : GalleryUiState {
        val isEmpty = images.isEmpty() && !hasMore
    }
    data class Error(val error: Throwable) : GalleryUiState
}
