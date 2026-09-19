package app.metrik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalMetrikColors.current

    var selectedTheme by remember { mutableStateOf(ThemeChoice.current) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            // ===== Theme =====
            Text(
                text = "Theme",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            ThemeOption(
                title = "Metrik Carbon",
                subtitle = "Graphite + cyan → lime gradient",
                selected = selectedTheme == MetrikTheme.CARBON,
                previewColors = listOf(Color(0xFF0B0D0E), Color(0xFF00E5FF), Color(0xFF00FF88))
            ) {
                selectedTheme = MetrikTheme.CARBON
                ThemeChoice.set(context, MetrikTheme.CARBON)
            }

            Spacer(Modifier.height(8.dp))

            ThemeOption(
                title = "AMOLED",
                subtitle = "Pure black — saves battery",
                selected = selectedTheme == MetrikTheme.AMOLED,
                previewColors = listOf(Color(0xFF000000), Color(0xFF4ADE80))
            ) {
                selectedTheme = MetrikTheme.AMOLED
                ThemeChoice.set(context, MetrikTheme.AMOLED)
            }

            Spacer(Modifier.height(8.dp))

            ThemeOption(
                title = "SM-F1N",
                subtitle = "Dark blue gradient",
                selected = selectedTheme == MetrikTheme.SMF1N,
                previewColors = listOf(Color(0xFF0A0E27), Color(0xFF4FACFE))
            ) {
                selectedTheme = MetrikTheme.SMF1N
                ThemeChoice.set(context, MetrikTheme.SMF1N)
            }

            Spacer(Modifier.height(8.dp))

            ThemeOption(
                title = "White",
                subtitle = "Light theme for weak devices",
                selected = selectedTheme == MetrikTheme.WHITE,
                previewColors = listOf(Color(0xFFF8F9FA), Color(0xFF007AFF))
            ) {
                selectedTheme = MetrikTheme.WHITE
                ThemeChoice.set(context, MetrikTheme.WHITE)
            }

            Spacer(Modifier.height(32.dp))

            // ===== About =====
            Text(
                text = "About",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SettingsRow("Version", "1.5.1")
                    SettingsRow("Build", "Metrik")
                    SettingsRow("Max score", "7 000 000")
                    SettingsRow("Min Android", "8.0 (API 26)")
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    previewColors: List<Color>,
    onClick: () -> Unit
) {
    val colors = LocalMetrikColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.surfaceVariant else colors.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.accent,
                    unselectedColor = colors.onSurfaceVariant
                )
            )

            Spacer(Modifier.width(8.dp))

            // Превью темы — цветной кружок
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        brush = Brush.linearGradient(previewColors),
                        shape = CircleShape
                    )
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    val colors = LocalMetrikColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = colors.onSurfaceVariant)
        Text(value, fontSize = 13.sp, color = colors.onSurface, fontWeight = FontWeight.Medium)
    }
}
