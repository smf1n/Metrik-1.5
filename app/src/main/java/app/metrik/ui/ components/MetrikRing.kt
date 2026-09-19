package app.metrik.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.metrik.ui.LocalMetrikColors

/**
 * Кольцо с тик-марками, как на спидометре.
 *
 * @param text Текст в центре
 * @param progress Прогресс 0..1 (0 = пусто, 1 = заполнено)
 * @param size Размер в dp
 * @param ticks Количество рисок по кольцу
 * @param onClick Если не null — кольцо кликабельно
 */
@Composable
fun MetrikRing(
    text: String,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    size: Int = 200,
    ticks: Int = 60,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalMetrikColors.current

    val infinite = rememberInfiniteTransition(label = "ring-pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring-scale"
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Box(
        modifier = modifier
            .size(size.dp)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val outerRadius = this.size.minDimension / 2 - 4.dp.toPx()
            val strokeWidth = 8.dp.toPx()
            val tickInnerRadius = outerRadius - strokeWidth - 6.dp.toPx()
            val tickOuterRadius = outerRadius - strokeWidth - 1.dp.toPx()

            drawCircle(
                color = colors.divider,
                radius = outerRadius - strokeWidth / 2,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            if (progress > 0f) {
                val sweep = 360f * progress.coerceIn(0f, 1f)
                drawArc(
                    color = colors.accentGradientStart,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - (outerRadius - strokeWidth / 2),
                        center.y - (outerRadius - strokeWidth / 2)
                    ),
                    size = Size(
                        (outerRadius - strokeWidth / 2) * 2,
                        (outerRadius - strokeWidth / 2) * 2
                    ),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            for (i in 0 until ticks) {
                val angleDeg = (360f / ticks) * i - 90f
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val cos = kotlin.math.cos(angleRad).toFloat()
                val sin = kotlin.math.sin(angleRad).toFloat()

                val startX = center.x + cos * tickInnerRadius
                val startY = center.y + sin * tickInnerRadius
                val endX = center.x + cos * tickOuterRadius
                val endY = center.y + sin * tickOuterRadius

                val isMajor = i % 5 == 0
                val isPassed = progress > 0f && (i.toFloat() / ticks) <= progress

                val tickColor = when {
                    isPassed -> colors.accentGradientEnd
                    isMajor -> colors.accent.copy(alpha = 0.7f)
                    else -> colors.onSurfaceVariant.copy(alpha = 0.5f)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.5f else 1.2f,
                    cap = StrokeCap.Round
                )
            }
        }

        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent
        )
    }
}
