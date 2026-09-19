package app.metrik.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.metrik.ui.LocalMetrikColors
import app.metrik.ui.MetrikTypography

/**
 * Одна строка "label — значение".
 * Значение в monospace, чтобы числа не "прыгали".
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalMetrikColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MetrikTypography.label(13),
            color = colors.onSurfaceVariant
        )

        Text(
            text = value,
            style = MetrikTypography.value(13),
            color = colors.onSurface,
            textAlign = TextAlign.End
        )
    }
}
