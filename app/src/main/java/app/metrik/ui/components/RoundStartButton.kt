package app.metrik.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.metrik.ui.LocalMetrikColors

/**
 * Большая круглая кнопка "START" в iOS-стиле.
 * Нажатие — запуск бенчмарка.
 */
@Composable
fun RoundStartButton(
    text: String,
    enabled: Boolean = true,
    size: Int = 160,
    onClick: () -> Unit
) {
    val colors = LocalMetrikColors.current

    Box(
        modifier = Modifier
            .size(size.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = colors.accent.copy(alpha = 0.4f),
                spotColor = colors.accent.copy(alpha = 0.6f)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accent,
                        colors.primary
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
            color = if (colors.isDark) colors.background else androidx.compose.ui.graphics.Color.White
        )
    }
}
