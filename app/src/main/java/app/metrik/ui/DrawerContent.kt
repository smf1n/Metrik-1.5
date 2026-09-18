package app.metrik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Боковое меню (drawer) слева.
 * Открывается кнопкой-гамбургером в левом верхнем углу.
 */
@Composable
fun DrawerContent(
    onNavigate: (String) -> Unit
) {
    val colors = LocalMetrikColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(vertical = 40.dp, horizontal = 16.dp)
    ) {
        // Заголовок
        Text(
            text = "Metrik",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Text(
            text = "Phone Benchmark",
            fontSize = 12.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(32.dp))

        // Пункты меню
        DrawerItem(
            icon = Icons.Default.Home,
            label = "Home",
            route = "home",
            onNavigate = onNavigate
        )

        DrawerItem(
            icon = Icons.Default.PlayArrow,
            label = "Benchmark",
            route = "bench",
            onNavigate = onNavigate
        )

        DrawerItem(
            icon = Icons.Default.List,
            label = "History",
            route = "history",
            onNavigate = onNavigate
        )

        Spacer(Modifier.weight(1f))

        // Нижний блок
        DrawerItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            route = "settings",
            onNavigate = onNavigate
        )

        DrawerItem(
            icon = Icons.Default.Info,
            label = "About",
            route = "about",
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    route: String,
    onNavigate: (String) -> Unit
) {
    val colors = LocalMetrikColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onNavigate(route) }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colors.onSurface,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
