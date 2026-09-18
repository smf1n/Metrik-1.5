package app.metrik.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================
// 3 темы Metrik: AMOLED / SM-F1N / White
// ============================================================

enum class MetrikTheme {
    AMOLED,
    SMF1N,
    WHITE
}

// Цвета для текущей темы (доступны через LocalMetrikColors.current)
data class MetrikColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val accent: Color,
    val divider: Color,
    val isDark: Boolean
)

// --- AMOLED: чистый чёрный, экономит батарею ---
private val AmoledColors = MetrikColors(
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF141414),
    primary = Color(0xFF4ADE80),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFE6EDF3),
    onSurfaceVariant = Color(0xFF9BA1A6),
    accent = Color(0xFF4ADE80),
    divider = Color(0xFF1F1F1F),
    isDark = true
)

// --- SM-F1N: тёмно-синий с градиентом ---
private val Smf1nColors = MetrikColors(
    background = Color(0xFF0A0E27),
    surface = Color(0xFF131A3A),
    surfaceVariant = Color(0xFF1A2145),
    primary = Color(0xFF4FACFE),
    onBackground = Color(0xFFE6EDF3),
    onSurface = Color(0xFFE6EDF3),
    onSurfaceVariant = Color(0xFFA0A8C0),
    accent = Color(0xFF4FACFE),
    divider = Color(0xFF2A3050),
    isDark = true
)

// --- White: светлая, для слабых устройств ---
private val WhiteColors = MetrikColors(
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F3F5),
    primary = Color(0xFF007AFF),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF6C757D),
    accent = Color(0xFF007AFF),
    divider = Color(0xFFE0E0E0),
    isDark = false
)

// CompositionLocal для доступа к цветам текущей темы
val LocalMetrikColors = staticCompositionLocalOf { AmoledColors }

// Провайдер темы
@Composable
fun MetrikTheme(
    theme: MetrikTheme = MetrikTheme.AMOLED,
    content: @Composable () -> Unit
) {
    val colors = when (theme) {
        MetrikTheme.AMOLED -> AmoledColors
        MetrikTheme.SMF1N -> Smf1nColors
        MetrikTheme.WHITE -> WhiteColors
    }

    // Преобразуем MetrikColors в Material3 colorScheme
    val materialColors = if (colors.isDark) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.background,
            secondary = colors.accent,
            background = colors.background,
            surface = colors.surface,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface,
            error = Color(0xFFEF4444)
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = Color.White,
            secondary = colors.accent,
            background = colors.background,
            surface = colors.surface,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface,
            error = Color(0xFFEF4444)
        )
    }

    CompositionLocalProvider(LocalMetrikColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
