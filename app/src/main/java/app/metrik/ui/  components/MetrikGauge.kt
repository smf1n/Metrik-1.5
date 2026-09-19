package app.metrik.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import app.metrik.ui.LocalMetrikColors
import app.metrik.ui.MetrikTypography

/**
 * Секторный индикатор прогресса (сегментами).
 *
 * @param progress 0..1
 * @param segments Сколько сегментов
 * @param label Подпись сверху слева
 * @param showPercent Показывать ли процент сверху справа
 */
@Composable
fun MetrikGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    segments: Int = 30,
    label: String? = null,
    showPercent: Boolean = true
) {
    val colors = LocalMetrikColors.current

    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "gauge-progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null || showPercent) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MetrikTypography.label(12),
                        color = colors.onSurfaceVariant
                    )
                } else {
                    Text(text = "", style = MetrikTypography.label(12))
                }

                if (showPercent) {
                    Text(
                        text = "${(animated * 100).toInt()}%",
                        style = MetrikTypography.value(12),
                        color = colors.accent
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val w = size.width
            val h = size.height
            val gap = 2.dp.toPx()
            val segWidth = (w - (segments - 1) * gap) / segments
            val filledCount = (animated * segments).toInt()
            val partial = (animated * segments) - filledCount

            for (i in 0 until segments) {
                val x = i * (segWidth + gap)
                val isFilled = i < filledCount
                val isPartial = i == filledCount && partial > 0f

                val segColor = when {
                    isFilled -> colors.accentGradientStart
                    isPartial -> colors.accentGradientStart.copy(alpha = partial)
                    else -> colors.divider
                }

                drawRoundRect(
                    color = segColor,
                    topLeft = Offset(x, 0f),
                    size = Size(segWidth, h),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }
    }
}
