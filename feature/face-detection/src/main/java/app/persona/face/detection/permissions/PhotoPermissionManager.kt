package app.persona.face.detection.permissions

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

object PhotoPermissionManager {
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

//    private fun getExtensionVersion(sdkVersion: Int): Int {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            Build.VERSION.EXTENSION_INT
//        } else {
//            0
//        }
//    }
} 