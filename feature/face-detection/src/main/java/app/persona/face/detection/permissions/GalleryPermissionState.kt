@file:OptIn(ExperimentalPermissionsApi::class)

package app.persona.face.detection.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Represents the different states of photo gallery permissions.
 * - Denied: No access to photos
 * - Granted: Full access to all photos
 * - Partial: Limited access to user-selected photos (Android 14+)
 */
sealed class PermissionStatus {
    data object Denied : PermissionStatus()
    data object Granted : PermissionStatus()
    data object Partial : PermissionStatus()
}

/**
 * Manages the state of photo gallery permissions in the app.
 * This class handles both the permission status and user interaction state.
 *
 * @property permissionsState The underlying permissions state from Accompanist
 * @property status Current status of photo access permissions
 * @property hasResponded Whether the user has responded to the permission request
 */
class GalleryPermissionState(
    private val permissionsState: MultiplePermissionsState,
    private val status: PermissionStatus,
    private val hasResponded: Boolean
) {
    /**
     * Whether the app has any form of access to photos (either full or partial)
     */
    val hasAccess: Boolean
        get() = status == PermissionStatus.Granted || status == PermissionStatus.Partial

    /**
     * Whether the app has partial (user-selected) access to photos.
     * This is only relevant for Android 14+ devices.
     */
    val isPartialAccess: Boolean
        get() = status == PermissionStatus.Partial

    /**
     * Whether to show the settings option instead of permission request.
     * This is true when the user has denied permissions and selected "Don't ask again"
     */
    val shouldShowSettings: Boolean
        get() = hasResponded && !permissionsState.shouldShowRationale

    /**
     * Launches the system permission request dialog
     */
    fun requestPermission() {
        permissionsState.launchMultiplePermissionRequest()
    }
}

/**
 * Creates and remembers a [GalleryPermissionState] instance.
 * This composable handles the permission state management and callbacks.
 *
 * @param onPermissionGranted Callback invoked when any form of photo access is granted
 * @return A [GalleryPermissionState] instance that can be used to manage photo permissions
 */
@Composable
fun rememberGalleryPermissionState(
    onPermissionGranted: () -> Unit
): GalleryPermissionState {
    val context = LocalContext.current
    var hasResponded by remember { mutableStateOf(false) }
    var status by remember {
        mutableStateOf<PermissionStatus>(PermissionStatus.Denied)
    }

    fun updatePermissionStatus() {
        status = when {
            hasFullAccess(context) -> PermissionStatus.Granted
            hasPartialAccess(context) -> PermissionStatus.Partial
            else -> PermissionStatus.Denied
        }

        if (status != PermissionStatus.Denied) {
            onPermissionGranted()
        }
    }

    val permissionsState = rememberPermissionState(
        onPermissionResult = { _ ->
            hasResponded = true
            updatePermissionStatus()
        }
    )

    // Observe changes in the permissions state
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        updatePermissionStatus()
    }

    return remember(permissionsState, status, hasResponded) {
        GalleryPermissionState(
            permissionsState = permissionsState,
            status = status,
            hasResponded = hasResponded
        )
    }
}

/**
 * Creates and remembers a [MultiplePermissionsState] for photo access.
 * This handles the different permission requirements across Android versions.
 *
 * @param onPermissionResult Callback for the permission request results
 * @return A [MultiplePermissionsState] configured for photo access permissions
 */
@Composable
fun rememberPermissionState(
    onPermissionResult: (Map<String, Boolean>) -> Unit = {}
): MultiplePermissionsState {
    val permissions = remember {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                listOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
            }

            else -> {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    return rememberMultiplePermissionsState(
        permissions = permissions,
        onPermissionsResult = { results -> onPermissionResult(results) }
    )
}

/**
 * Checks if the app has full access to device photos.
 *
 * @param context The application context
 * @return true if the app has full photo access permission:
 *         - For Android 13+ (Tiramisu): READ_MEDIA_IMAGES permission
 *         - For older versions: READ_EXTERNAL_STORAGE permission
 */
fun hasFullAccess(context: Context): Boolean {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PermissionChecker.PERMISSION_GRANTED
        }

        else -> {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PermissionChecker.PERMISSION_GRANTED
        }
    }
}

/**
 * Checks if the app has partial (user-selected) access to photos.
 * This is only available on Android 14+ (UPSIDE_DOWN_CAKE).
 *
 * @param context The application context
 * @return true if the app has permission to access user-selected photos,
 *         false for Android versions below 14 or if permission not granted
 */
fun hasPartialAccess(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) == PermissionChecker.PERMISSION_GRANTED
    } else {
        false
    }
}
