package app.metrik.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.metrik.ui.LocalMetrikColors
import app.metrik.ui.MetrikTypography

/**
 * Шкала с тик-марками (как на приборе).
 *
 * @param value Текущее значение 0..max
 * @param max Максимум
 * @param ticks Количество рисок
 * @param label Подпись под шкалой (опционально)
 */
@Composable
fun MetricBar(
    value: Float,
    max: Float,
    modifier: Modifier = Modifier,
    ticks: Int = 20,
    height: Dp = 12.dp,
    label: String? = null
) {
    val colors = LocalMetrikColors.current
    val ratio = if (max <= 0f) 0f else (value / max).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val w = size.width
            val h = size.height
            val tickStep = w / (ticks - 1)
            val filled = w * ratio

            // Фоновая линия
            drawLine(
                color = colors.divider,
                start = Offset(0f, h / 2),
                end = Offset(w, h / 2),
                strokeWidth = h / 3,
                cap = StrokeCap.Round
            )

            // Заполненная часть — градиент через две линии
            if (ratio > 0f) {
                drawLine(
                    color = colors.accentGradientStart,
                    start = Offset(0f, h / 2),
                    end = Offset(filled, h / 2),
                    strokeWidth = h,
                    cap = StrokeCap.Round
                )
                // Верхняя половина — второй цвет градиента
                drawLine(
                    color = colors.accentGradientEnd,
                    start = Offset(0f, h / 2),
                    end = Offset(filled, h / 2),
                    strokeWidth = h / 2,
                    cap = StrokeCap.Round
                )
            }

            // Риски
            for (i in 0 until ticks) {
                val x = i * tickStep
                val isPassed = x <= filled
                drawLine(
                    color = if (isPassed) colors.accentGradientEnd.copy(alpha = 0.9f)
                            else colors.onSurfaceVariant.copy(alpha = 0.4f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.5f
                )
            }
        }

        if (label != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MetrikTypography.label(10),
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = "${(ratio * 100).toInt()}%",
                    style = MetrikTypography.value(10),
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
