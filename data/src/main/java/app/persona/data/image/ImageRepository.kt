package app.persona.data.image

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository responsible for accessing device's media storage and retrieving images.
 * Implements efficient batch loading to prevent memory issues with large galleries.
 * Supports Android's photo permission system, including partial access in Android 14+.
 */
@Singleton
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Provides a flow of image batches from the device's media store.
     * Uses batching to efficiently handle large galleries and prevent OOM errors.
     *
     * @param batchSize Number of images to load in each batch
     * @param startIndex Starting position for pagination
     * @return Flow of [ImageBatch] containing URIs and metadata for found images
     */
    fun getImagesStream(batchSize: Int = 20, startIndex: Int = 0): Flow<ImageBatch> = flow {
        context.contentResolver.query(
            Media.EXTERNAL_CONTENT_URI,
            PROJECTION,
            createQueryArgs(),
            null
        )?.use { cursor ->
            var currentIndex = startIndex
            while (!cursor.isAfterLast) {
                val batch = processImageBatch(cursor, currentIndex, batchSize)
                emit(batch)
                currentIndex = batch.nextIndex
                if (!batch.hasMore) break
            }
        }
    }

    /**
     * Processes a batch of images from the cursor.
     * Extracts necessary metadata while maintaining memory efficiency.
     *
     * @param cursor Database cursor containing image metadata
     * @param startIndex Starting position in the cursor
     * @param batchSize Maximum number of images to process
     * @return [ImageBatch] containing processed images and pagination info
     */
    private suspend fun processImageBatch(
        cursor: Cursor,
        startIndex: Int,
        batchSize: Int
    ): ImageBatch = withContext(Dispatchers.IO) {
        if (startIndex > 0) {
            cursor.moveToPosition(startIndex - 1)
        }

        val images = mutableListOf<ImageData>()
        var processedCount = 0
        val idColumn = cursor.getColumnIndexOrThrow(Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(Media.DISPLAY_NAME)

        while (cursor.moveToNext() && processedCount < batchSize) {
            val id = cursor.getLong(idColumn)
            val fileName = cursor.getString(nameColumn)
            val contentUri = Uri.withAppendedPath(
                Media.EXTERNAL_CONTENT_URI,
                id.toString()
            )

            images.add(ImageData(id = id, uri = contentUri, fileName = fileName))
            processedCount++
        }

        val hasMore = processedCount == batchSize && cursor.moveToNext()
        if (hasMore) cursor.moveToPrevious()

        ImageBatch(
            images = images,
            hasMore = hasMore,
            nextIndex = startIndex + processedCount
        )
    }

    /**
     * Creates query arguments for MediaStore content resolver queries.
     * Specifically handles Android 14+ photo picker functionality.
     *
     * @return Bundle with query arguments for Android 14+, null for older versions
     */
    private fun createQueryArgs(): Bundle? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Bundle().apply {
                putString(
                    ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                    "${MediaStore.MediaColumns.DATE_ADDED} DESC"
                )
            }
        } else {
            null
        }
    }

    companion object {
        /** Columns to retrieve from MediaStore */
        private val PROJECTION = arrayOf(Media._ID, Media.DISPLAY_NAME)
    }
}

/**
 * Represents a batch of images loaded from the media store.
 * Used for pagination and memory-efficient loading of large galleries.
 *
 * @property images List of images in this batch
 * @property hasMore Whether more images are available after this batch
 * @property nextIndex Starting index for the next batch
 */
data class ImageBatch(
    val images: List<ImageData>,
    val hasMore: Boolean,
    val nextIndex: Int
)

/**
 * Contains metadata for a single image from the media store.
 *
 * @property id MediaStore ID of the image
 * @property uri Content URI for accessing the image
 * @property fileName Display name of the image file
 */
data class ImageData(
    val id: Long,
    val uri: Uri,
    val fileName: String
)
