package app.metrik.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.metrik.data.*
import app.metrik.ui.components.InfoCard
import app.metrik.ui.components.InfoRow
import app.metrik.ui.components.MetrikRing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onOpenDrawer: () -> Unit,
    onStartBench: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalMetrikColors.current

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
            Spacer(Modifier.height(16.dp))

            // ===== Кнопка START (Metrik Ring) =====
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MetrikRing(
                    text = "START",
                    size = 200,
                    ticks = 60,
                    onClick = onStartBench
                )
            }

            Spacer(Modifier.height(32.dp))

            // ===== CPU =====
            InfoCard(title = "CPU", icon = Icons.Default.Memory) {
                InfoRow("SoC", profile.soc.ifEmpty { "Unknown" })
                InfoRow("Cores", "${profile.cpuCores}")
                InfoRow("ABI", profile.cpuAbi)
                InfoRow("Min freq", CpuReader.formatFreq(profile.cpuMinFreq))
                InfoRow("Max freq", CpuReader.formatFreq(profile.cpuMaxFreq))
                InfoRow("Current", CpuReader.formatFreq(profile.cpuCurrentFreq))
                if (profile.cpuTemp > 0) {
                    InfoRow("Temp", String.format("%.1f°C", profile.cpuTemp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // ===== RAM =====
            InfoCard(title = "RAM", icon = Icons.Default.Speed) {
                InfoRow("Total", StorageReader.formatBytes(profile.ram.total))
                InfoRow("Free", StorageReader.formatBytes(profile.ram.available))
                InfoRow("Used", StorageReader.formatBytes(profile.ram.used))
                InfoRow(
                    "Load",
                    String.format(
                        "%.0f%%",
                        profile.ram.used.toDouble() / profile.ram.total.coerceAtLeast(1) * 100
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // ===== Storage =====
            InfoCard(title = "Storage", icon = Icons.Default.SdStorage) {
                InfoRow("Total", StorageReader.formatBytes(profile.storage.total))
                InfoRow("Free", StorageReader.formatBytes(profile.storage.free))
                InfoRow("Used", StorageReader.formatBytes(profile.storage.used))
                InfoRow("Usage", StorageReader.formatUsedPercent(profile.storage))
            }

            Spacer(Modifier.height(12.dp))

            // ===== Battery =====
            InfoCard(title = "Battery", icon = Icons.Default.BatteryFull) {
                InfoRow("Level", "${profile.battery.level}%")
                InfoRow("Voltage", String.format("%.2f V", profile.battery.voltage / 1000f))
                InfoRow("Temp", String.format("%.1f°C", profile.battery.temp))
                if (profile.battery.current != 0) {
                    InfoRow("Current", "${profile.battery.current} mA")
                }
                InfoRow("Health", profile.battery.health)
                InfoRow("Status", profile.battery.status)
                if (profile.battery.technology.isNotEmpty()) {
                    InfoRow("Technology", profile.battery.technology)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ===== Display =====
            InfoCard(title = "Display", icon = Icons.Default.PhoneAndroid) {
                InfoRow(
                    "Resolution",
                    DisplayReader.formatResolution(
                        profile.display.width,
                        profile.display.height
                    )
                )
                InfoRow(
                    "Density",
                    DisplayReader.formatDensity(profile.display.densityDpi)
                )
                InfoRow("Diagonal", DisplayReader.formatDiagonal(profile.display.diagonal))
                InfoRow("Refresh", DisplayReader.formatRefresh(profile.display.refreshRate))
            }

            Spacer(Modifier.height(12.dp))

            // ===== GPU =====
            InfoCard(title = "GPU", icon = Icons.Default.Videocam) {
                InfoRow("Renderer", GpuReader.renderer())
                InfoRow("Vendor", GpuReader.vendorFromRenderer())
                InfoRow("OpenGL", GpuReader.version())
            }

            Spacer(Modifier.height(12.dp))

            // ===== System =====
            InfoCard(title = "System", icon = Icons.Default.Storage) {
                InfoRow("Android", profile.androidVersion)
                InfoRow("API", "${profile.apiLevel}")
                InfoRow("Kernel", profile.kernelVersion)
                InfoRow("Build", profile.buildId)
                InfoRow("Uptime", profile.uptime)
                InfoRow("Locale", profile.locale)
                InfoRow("Timezone", profile.timezone)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
