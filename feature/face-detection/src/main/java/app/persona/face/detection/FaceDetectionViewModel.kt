package app.persona.face.detection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.persona.data.detection.FaceDetection
import app.persona.data.detection.ProcessedImageWithBitmap
import app.persona.domain.DetectFacesUseCase
import app.persona.face.detection.GalleryUiState.Error
import app.persona.face.detection.GalleryUiState.Initial
import app.persona.face.detection.GalleryUiState.Loading
import app.persona.face.detection.GalleryUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val detectFacesUseCase: DetectFacesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<GalleryUiState>(Initial)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private var currentIndex = 0
    private var isProcessing = false

    /**
     * Initiates or continues the image scanning process.
     * @param reset If true, resets the scanning progress and starts from the beginning
     */
    fun scanImages(reset: Boolean = false) {
        if (isProcessing) return
        if (reset) resetState()

        viewModelScope.launch {
            isProcessing = true
            if (_uiState.value is Initial) {
                _uiState.value = Loading
            }

            try {
                detectFacesUseCase(startIndex = currentIndex).collect { result ->
                    result.fold(
                        onSuccess = { update ->
                            currentIndex = update.nextIndex
                            updateUiState(update.image, update.hasMore)
                        },
                        onFailure = { error ->
                            handleError(error)
                        }
                    )
                }
                
                // Ensure we're in Success state after flow completes
                if (_uiState.value is Loading) {
                    _uiState.value = Success(emptyList(), hasMore = false)
                }
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

    /**
     * Updates the name of a specific face detection
     */
    fun updateFaceName(face: FaceDetection, newName: String) {
        val currentState = _uiState.value as? Success ?: return

        _uiState.value = currentState.copy(
            images = currentState.images.map { processedImage ->
                processedImage.copy(
                    detections = processedImage.detections?.map { detection ->
                        if (detection === face) detection.copy(name = newName) else detection
                    }
                )
            }
        )
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
        _uiState.value = Error(error)
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
