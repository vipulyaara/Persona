package app.persona.face.detection.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.persona.data.detection.FaceDetection
import app.persona.data.detection.ProcessedImageWithBitmap
import app.persona.theme.Dimens

@Composable
fun GalleryGrid(
    images: List<ProcessedImageWithBitmap>,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onFaceNameUpdated: (FaceDetection, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(hasMore) {
        if (hasMore) {
            onLoadMore()
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing08),
        verticalItemSpacing = Dimens.Spacing08,
    ) {
        items(images) { processedImage ->
            ImageWithFace(
                bitmap = processedImage.bitmap,
                faceCount = processedImage.faceCount,
                aspectRatio = processedImage.aspectRatio,
                detections = processedImage.detections,
                onFaceNameUpdated = onFaceNameUpdated
            )
        }

        if (hasMore) {
            items(PlaceholderCount) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(Dimens.Spacing12))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                )
            }
        }
    }
}

const val PlaceholderCount = 6