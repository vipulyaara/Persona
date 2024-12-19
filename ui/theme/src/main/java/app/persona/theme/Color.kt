package app.persona.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun colorScheme(
    useDarkColors: Boolean,
    useMaterialYou: Boolean = false
): ColorScheme = when {
    useMaterialYou && isAtLeastS() && useDarkColors -> {
        dynamicDarkColorScheme(LocalContext.current)
    }

    useMaterialYou && isAtLeastS() && !useDarkColors -> {
        dynamicLightColorScheme(LocalContext.current)
    }

    useDarkColors -> DarkAppColors
    else -> LightAppColors
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isAtLeastS(): Boolean {
    return Build.VERSION.SDK_INT >= 31
}

val LightAppColors = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF000000),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color(0xFFFFFFFF),

    tertiary = Color(0xFF000000),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF333333),
    onTertiaryContainer = Color(0xFFFFFFFF),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),

    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF000000),

    error = Color(0xFF000000),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFE6E6E6),
    onErrorContainer = Color(0xFF000000),

    outline = Color(0xFF666666),
    surfaceTint = Color(0xFF000000),

    inverseSurface = Color(0xFF000000),
    inverseOnSurface = Color(0xFFFFFFFF),
)

val DarkAppColors = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFFCCCCCC),
    onPrimaryContainer = Color(0xFF000000),
    inversePrimary = Color(0xFF000000),

    secondary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFCCCCCC),
    onSecondaryContainer = Color(0xFF000000),

    tertiary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFCCCCCC),
    onTertiaryContainer = Color(0xFF000000),

    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),

    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),

    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFFFFFFF),

    error = Color(0xFFFFFFFF),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF333333),
    onErrorContainer = Color(0xFFFFFFFF),

    outline = Color(0xFF999999),
    surfaceTint = Color(0xFFFFFFFF)
)
