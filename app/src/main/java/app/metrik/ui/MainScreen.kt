package app.metrik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.metrik.data.*
import app.metrik.ui.components.InfoCard
import app.metrik.ui.components.InfoRow
import app.metrik.ui.components.RoundStartButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenDrawer: () -> Unit,
    onStartBench: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalMetrikColors.current

    // Сканируем устройство один раз
    val profile by remember { mutableStateOf(DeviceScanner.scan(context)) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Metrik",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                        Text(
                            text = profile.displayName(),
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ===== Кнопка НАЧАТЬ =====
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RoundStartButton(
                    text = "START",
                    size = 160,
                    onClick = onStartBench
                )
            }

            Spacer(Modifier.height(24.dp))

            // ===== CPU =====
            InfoCard(title = "🔲 CPU") {
                InfoRow("SoC", profile.soc.ifEmpty { "Unknown" })
                InfoRow("Ядра", "${profile.cpuCores}")
                InfoRow("Архитектура", profile.cpuAbi)
                InfoRow("Мин. частота", CpuReader.formatFreq(profile.cpuMinFreq))
                InfoRow("Макс. частота", CpuReader.formatFreq(profile.cpuMaxFreq))
                InfoRow("Текущая", CpuReader.formatFreq(profile.cpuCurrentFreq))
                if (profile.cpuTemp > 0) {
                    InfoRow("Температура", String.format("%.1f°C", profile.cpuTemp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // ===== RAM =====
            InfoCard(title = "💾 RAM") {
                InfoRow("Всего", StorageReader.formatBytes(profile.ram.total))
                InfoRow("Свободно", StorageReader.formatBytes(profile.ram.available))
                InfoRow("Занято", StorageReader.formatBytes(profile.ram.used))
                InfoRow(
                    "Загрузка",
                    String.format(
                        "%.0f%%",
                        profile.ram.used.toDouble() / profile.ram.total.coerceAtLeast(1) * 100
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // ===== Storage =====
            InfoCard(title = "💿 Storage") {
                InfoRow("Всего", StorageReader.formatBytes(profile.storage.total))
                InfoRow("Свободно", StorageReader.formatBytes(profile.storage.free))
                InfoRow("Занято", StorageReader.formatBytes(profile.storage.used))
                InfoRow("Использовано", StorageReader.formatUsedPercent(profile.storage))
            }

            Spacer(Modifier.height(12.dp))

            // ===== Battery =====
            InfoCard(title = "🔋 Battery") {
                InfoRow("Уровень", "${profile.battery.level}%")
                InfoRow("Напряжение", String.format("%.2f В", profile.battery.voltage / 1000f))
                InfoRow("Температура", String.format("%.1f°C", profile.battery.temp))
                if (profile.battery.current != 0) {
                    InfoRow("Ток", "${profile.battery.current} мА")
                }
                InfoRow("Здоровье", profile.battery.health)
                InfoRow("Статус", profile.battery.status)
                if (profile.battery.technology.isNotEmpty()) {
                    InfoRow("Технология", profile.battery.technology)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ===== Display =====
            InfoCard(title = "📺 Display") {
                InfoRow(
                    "Разрешение",
                    DisplayReader.formatResolution(
                        profile.display.width,
                        profile.display.height
                    )
                )
                InfoRow(
                    "Плотность",
                    DisplayReader.formatDensity(profile.display.densityDpi)
                )
                InfoRow("Диагональ", DisplayReader.formatDiagonal(profile.display.diagonal))
                InfoRow("Частота", DisplayReader.formatRefresh(profile.display.refreshRate))
            }

            Spacer(Modifier.height(12.dp))

            // ===== GPU =====
            InfoCard(title = "🎮 GPU") {
                InfoRow("Рендерер", GpuReader.renderer())
                InfoRow("Вендор", GpuReader.vendorFromRenderer())
                InfoRow("OpenGL", GpuReader.version())
            }

            Spacer(Modifier.height(12.dp))

            // ===== System =====
            InfoCard(title = "📦 System") {
                InfoRow("Android", profile.androidVersion)
                InfoRow("API", "${profile.apiLevel}")
                InfoRow("Ядро", profile.kernelVersion)
                InfoRow("Build", profile.buildId)
                InfoRow("Uptime", profile.uptime)
                InfoRow("Локаль", profile.locale)
                InfoRow("Часовой пояс", profile.timezone)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
