package app.persona.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Images.Media
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Responsible for loading and optimizing images from the device's storage.
 * Implements memory-efficient bitmap loading with automatic resizing for large images.
 * Key features:
 * - Handles API version compatibility for bitmap decoding
 * - Automatically scales large images to prevent OOM errors
 * - Maintains aspect ratio for accurate face detection
 * - Manages bitmap lifecycle to prevent memory leaks
 */
@Singleton
class BitmapLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Loads and optimizes a bitmap from the given URI.
     * The process includes:
     * 1. Loading the bitmap using the appropriate API for the device version
     * 2. Resizing if dimensions exceed [MAX_DIMENSION]
     * 3. Cleaning up resources to prevent memory leaks
     *
     * @param uri Source URI of the image to load
     * @return Result containing the optimized bitmap or an error
     */
    suspend fun loadBitmap(uri: Uri): Result<Bitmap> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    ImageDecoder.createSource(context.contentResolver, uri).let { source ->
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            // Software rendering provides better compatibility across devices
                            // and more predictable memory usage
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        }
                    }
                }

                else -> {
                    @Suppress("DEPRECATION")
                    Media.getBitmap(context.contentResolver, uri)
                }
            }

            // Calculate optimal dimensions while preserving aspect ratio
            val (newWidth, newHeight) = calculateResizedDimensions(
                originalWidth = bitmap.width,
                originalHeight = bitmap.height,
                maxDimension = MAX_DIMENSION
            )

            // Only resize if necessary to avoid unnecessary memory allocation
            if (bitmap.width > MAX_DIMENSION || bitmap.height > MAX_DIMENSION) {
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).also {
                    // Immediately recycle the original bitmap to free memory
                    // This is crucial for processing large batches of images
                    if (it != bitmap) bitmap.recycle()
                }
            } else {
                bitmap
            }
        }.onFailure { error ->
            error.printStackTrace()
        }
    }

    /**
     * Calculates optimal dimensions for resizing an image while maintaining aspect ratio.
     * The algorithm ensures that:
     * 1. The larger dimension is scaled to [maxDimension]
     * 2. The smaller dimension is scaled proportionally
     * 3. Original dimensions are preserved if already within limits
     *
     * This approach maintains image quality while significantly reducing memory usage
     * for large images, which is crucial for batch processing.
     */
    @Suppress("SameParameterValue")
    private fun calculateResizedDimensions(
        originalWidth: Int,
        originalHeight: Int,
        maxDimension: Int
    ): Pair<Int, Int> {
        return when {
            // Keep original dimensions if already within limits
            originalWidth <= maxDimension && originalHeight <= maxDimension -> {
                originalWidth to originalHeight
            }
            // Scale based on the larger dimension while maintaining aspect ratio
            originalWidth > originalHeight -> {
                maxDimension to (originalHeight * (maxDimension.toFloat() / originalWidth)).toInt()
            }

            else -> {
                (originalWidth * (maxDimension.toFloat() / originalHeight)).toInt() to maxDimension
            }
        }
    }

    companion object {
        private const val MAX_DIMENSION = 1080
    }
}
