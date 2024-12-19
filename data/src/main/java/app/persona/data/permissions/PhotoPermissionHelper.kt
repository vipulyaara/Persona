package app.persona.data.permissions

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

/**
 * Manages photo-related permissions across different Android API levels.
 * Handles both full access and partial (user-selected) access to device photos.
 */
object PhotoPermissionHelper {
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

    /**
     * Creates query arguments for MediaStore content resolver queries.
     * Specifically handles Android 14+ photo picker functionality.
     *
     * @param onlyLatestSelection If true, only returns photos from the most recent photo picker selection
     * @return Bundle with query arguments for Android 14+, null for older versions
     */
    fun createQueryArgs(onlyLatestSelection: Boolean = false): Bundle? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Bundle().apply {
                putString(
                    ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                    "${MediaStore.MediaColumns.DATE_ADDED} DESC"
                )
                if (onlyLatestSelection) {

                    putBoolean("android:query-arg-latest-selection-only", true)
                }
            }
        } else {
            null
        }
    }
}