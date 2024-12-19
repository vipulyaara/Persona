package app.persona.domain

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import app.cash.turbine.test
import app.persona.data.detection.FaceDetection
import app.persona.data.detection.FaceDetectorHelper
import app.persona.data.image.BitmapLoader
import app.persona.data.image.ImageBatch
import app.persona.data.image.ImageData
import app.persona.data.image.ImageRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DetectFacesUseCaseTest {
    
    private lateinit var imageRepository: ImageRepository
    private lateinit var bitmapLoader: BitmapLoader
    private lateinit var faceDetectorHelper: FaceDetectorHelper
    private lateinit var detectFacesUseCase: DetectFacesUseCase

    @Before
    fun setup() {
        imageRepository = mock()
        bitmapLoader = mock()
        faceDetectorHelper = mock()
        detectFacesUseCase = DetectFacesUseCase(
            imageRepository = imageRepository,
            bitmapLoader = bitmapLoader,
            faceDetectorHelper = faceDetectorHelper
        )
    }

    @Test
    fun `successfully process batch with faces detected`() = runTest {
        // Given
        val testUri = mock<Uri>()
        val testBitmap = mock<Bitmap>()
        val imageBatch = ImageBatch(
            images = listOf(
                ImageData(id = 1L, uri = testUri, fileName = "test.jpg")
            ),
            hasMore = false,
            nextIndex = 1
        )

        val faceDetection = FaceDetection(
            boundingBox = RectF(0f, 0f, 100f, 100f),
            confidence = 0.95f
        )

        whenever(imageRepository.getImagesStream(any(), any())).thenReturn(
            flowOf(imageBatch)
        )
        whenever(bitmapLoader.loadBitmap(any())).thenReturn(
            Result.success(testBitmap)
        )
        whenever(faceDetectorHelper.detectImage(any())).thenReturn(
            listOf(faceDetection)
        )
        whenever(testBitmap.width).thenReturn(100)
        whenever(testBitmap.height).thenReturn(100)

        // When & Then
        detectFacesUseCase(startIndex = 0).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            
            val update = result.getOrNull()
            assertEquals(1, update?.image?.faceCount)
            assertEquals(false, update?.hasMore)
            assertEquals(1, update?.nextIndex)
            
            awaitComplete()
        }
    }

    @Test
    fun `return empty result when no faces detected`() = runTest {
        // Given
        val testUri = mock<Uri>()
        val testBitmap = mock<Bitmap>()
        val imageBatch = ImageBatch(
            images = listOf(
                ImageData(id = 1L, uri = testUri, fileName = "test.jpg")
            ),
            hasMore = false,
            nextIndex = 1
        )

        whenever(imageRepository.getImagesStream(any(), any())).thenReturn(
            flowOf(imageBatch)
        )
        whenever(bitmapLoader.loadBitmap(any())).thenReturn(
            Result.success(testBitmap)
        )
        whenever(faceDetectorHelper.detectImage(any())).thenReturn(
            emptyList()
        )
        whenever(testBitmap.width).thenReturn(100)
        whenever(testBitmap.height).thenReturn(100)

        // When & Then
        detectFacesUseCase(startIndex = 0).test {
            awaitComplete()
        }
    }

    @Test
    fun `handle bitmap loading failure`() = runTest {
        // Given
        val testUri = mock<Uri>()
        val imageBatch = ImageBatch(
            images = listOf(
                ImageData(id = 1L, uri = testUri, fileName = "test.jpg")
            ),
            hasMore = false,
            nextIndex = 1
        )

        whenever(imageRepository.getImagesStream(any(), any())).thenReturn(
            flowOf(imageBatch)
        )
        whenever(bitmapLoader.loadBitmap(any())).thenReturn(
            Result.failure(Exception("Failed to load bitmap"))
        )

        // When & Then
        detectFacesUseCase(startIndex = 0).test {
            awaitComplete()
        }
    }
} 