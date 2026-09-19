package app.metrik.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.metrik.ui.LocalMetrikColors

/**
 * Круглая кнопка START с градиентом и пульсацией.
 */
@Composable
fun RoundStartButton(
    text: String,
    enabled: Boolean = true,
    size: Int = 160,
    onClick: () -> Unit
) {
    val colors = LocalMetrikColors.current

    // Пульсация
    val infinite = rememberInfiniteTransition(label = "start-pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-scale"
    )

    val scale = if (enabled) pulse else 1.0f

    Box(
        modifier = Modifier
            .size(size.dp)
            .scale(scale)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = colors.accent.copy(alpha = 0.5f),
                spotColor = colors.accentGradientEnd.copy(alpha = 0.6f)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentGradientStart,
                        colors.accentGradientEnd
                    )
                ),
                shape = CircleShape
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (colors.isDark) colors.background else Color.White
        )
    }
}
