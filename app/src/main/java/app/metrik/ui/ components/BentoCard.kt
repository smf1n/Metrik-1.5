package app.metrik.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.metrik.ui.LocalMetrikColors
import app.metrik.ui.MetrikTypography

/**
 * Bento-плитка с заголовком, значением и опциональной подписью.
 */
@Composable
fun BentoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accentValue: Boolean = false,
    valueSize: Int = 28,
    cornerRadius: Dp = 20.dp
) {
    val colors = LocalMetrikColors.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = title.uppercase(),
                    style = MetrikTypography.label(11),
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                style = MetrikTypography.score(valueSize),
                color = if (accentValue) colors.accent else colors.onSurface
            )

            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MetrikTypography.value(11),
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Bento-плитка с дополнительным списком строк.
 * Для категорий CPU / Storage / Battery.
 */
@Composable
fun BentoCardWithRows(
    title: String,
    value: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accentValue: Boolean = false,
    valueSize: Int = 24,
    cornerRadius: Dp = 20.dp
) {
    val colors = LocalMetrikColors.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = title.uppercase(),
                        style = MetrikTypography.label(11),
                        color = colors.onSurfaceVariant
                    )
                }

                Text(
                    text = value,
                    style = MetrikTypography.score(valueSize),
                    color = if (accentValue) colors.accent else colors.onSurface
                )
            }

            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MetrikTypography.value(11),
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))

            rows.forEach { (label, rowValue) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MetrikTypography.label(12),
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = rowValue,
                        style = MetrikTypography.value(12),
                        color = colors.onSurface
                    )
                }
            }
        }
    }
}
