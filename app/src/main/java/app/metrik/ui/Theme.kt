package app.metrik.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================================
// 4 темы Metrik: CARBON (default) / AMOLED / SM-F1N / White
// ============================================================

enum class MetrikTheme {
    CARBON,
    AMOLED,
    SMF1N,
    WHITE
}

data class MetrikColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val accent: Color,
    val accentGradientStart: Color,
    val accentGradientEnd: Color,
    val divider: Color,
    val danger: Color,
    val success: Color,
    val isDark: Boolean
)

// --- CARBON: глубокий графит + cyan→lime градиент (ФИРМЕННАЯ) ---
private val CarbonColors = MetrikColors(
    background = Color(0xFF0B0D0E),
    surface = Color(0xFF141719),
    surfaceVariant = Color(0xFF1A1F22),
    primary = Color(0xFF00E5FF),           // cyan
    onBackground = Color(0xFFE8EBED),
    onSurface = Color(0xFFE8EBED),
    onSurfaceVariant = Color(0xFF7A8288),
    accent = Color(0xFF00E5FF),
    accentGradientStart = Color(0xFF00E5FF),
    accentGradientEnd = Color(0xFF00FF88),
    divider = Color(0xFF1F2428),
    danger = Color(0xFFFF3B5C),
    success = Color(0xFF00FF88),
    isDark = true
)

// --- AMOLED: чистый чёрный ---
private val AmoledColors = MetrikColors(
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF141414),
    primary = Color(0xFF4ADE80),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFE6EDF3),
    onSurfaceVariant = Color(0xFF9BA1A6),
    accent = Color(0xFF4ADE80),
    accentGradientStart = Color(0xFF4ADE80),
    accentGradientEnd = Color(0xFF22C55E),
    divider = Color(0xFF1F1F1F),
    danger = Color(0xFFEF4444),
    success = Color(0xFF4ADE80),
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
    accentGradientStart = Color(0xFF4FACFE),
    accentGradientEnd = Color(0xFF00C6FB),
    divider = Color(0xFF2A3050),
    danger = Color(0xFFEF4444),
    success = Color(0xFF4ADE80),
    isDark = true
)

// --- White: светлая ---
private val WhiteColors = MetrikColors(
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F3F5),
    primary = Color(0xFF007AFF),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF6C757D),
    accent = Color(0xFF007AFF),
    accentGradientStart = Color(0xFF007AFF),
    accentGradientEnd = Color(0xFF00C6FB),
    divider = Color(0xFFE0E0E0),
    danger = Color(0xFFDC2626),
    success = Color(0xFF16A34A),
    isDark = false
)

val LocalMetrikColors = staticCompositionLocalOf { CarbonColors }

// ============================================================
// ТИПОГРАФИКА: monospace для чисел
// ============================================================

object MetrikTypography {
    val mono = FontFamily.Monospace
    val sans = FontFamily.SansSerif

    /** Стиль для больших чисел (баллы, результаты) */
    fun score(size: Int = 56): TextStyle = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Bold,
        fontSize = size.sp,
        letterSpacing = (-1).sp
    )

    /** Стиль для чисел в строках (значения, МБ/с, °C) */
    fun value(size: Int = 13): TextStyle = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Medium,
        fontSize = size.sp
    )

    /** Стиль для заголовков (не чисел) */
    fun heading(size: Int = 16): TextStyle = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Bold,
        fontSize = size.sp
    )

    /** Стиль для меток/label */
    fun label(size: Int = 12): TextStyle = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Normal,
        fontSize = size.sp,
        letterSpacing = 0.5.sp
    )
}

// ============================================================
// ПРОВАЙДЕР ТЕМЫ
// ============================================================

@Composable
fun MetrikTheme(
    theme: MetrikTheme = MetrikTheme.CARBON,
    content: @Composable () -> Unit
) {
    val colors = when (theme) {
        MetrikTheme.CARBON -> CarbonColors
        MetrikTheme.AMOLED -> AmoledColors
        MetrikTheme.SMF1N -> Smf1nColors
        MetrikTheme.WHITE -> WhiteColors
    }

    val materialColors = if (colors.isDark) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.background,
            secondary = colors.accent,
            background = colors.background,
            surface = colors.surface,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface,
            error = colors.danger
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
            error = colors.danger
        )
    }

    CompositionLocalProvider(LocalMetrikColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}

// ============================================================
// ГРАДИЕНТНЫЕ ХЕЛПЕРЫ
// ============================================================

fun gradientBrush(colors: MetrikColors): Brush = Brush.horizontalGradient(
    colors = listOf(colors.accentGradientStart, colors.accentGradientEnd)
)

fun radialGradientBrush(colors: MetrikColors): Brush = Brush.radialGradient(
    colors = listOf(colors.accentGradientStart, colors.accentGradientEnd)
)
