package app.metrik.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.metrik.bench.*
import app.metrik.data.BatteryReader
import app.metrik.data.CpuReader
import app.metrik.ui.components.BentoCard
import app.metrik.ui.components.BentoCardWithRows
import app.metrik.ui.components.MetrikGauge
import app.metrik.util.Preferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalMetrikColors.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isRunning by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0) }
    var result by remember { mutableStateOf<BenchResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    var liveCpuTemp by remember { mutableStateOf(0f) }
    var liveBatteryTemp by remember { mutableStateOf(0f) }
    var liveCpuFreq by remember { mutableStateOf(0L) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            liveCpuTemp = CpuReader.temperature()
            liveBatteryTemp = BatteryReader.temperature(context)
            liveCpuFreq = CpuReader.averageCurrentFreq()
            kotlinx.coroutines.delay(500)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Benchmark",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isRunning) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (isRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = currentStep,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )

                        Spacer(Modifier.height(16.dp))

                        MetrikGauge(
                            progress = progress / 100f,
                            segments = 30,
                            label = null,
                            showPercent = true
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = "Live",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                        Spacer(Modifier.height(8.dp))

                        LiveRow("CPU temp", if (liveCpuTemp > 0) String.format("%.1f°C", liveCpuTemp) else "N/A")
                        LiveRow("Battery temp", String.format("%.1f°C", liveBatteryTemp))
                        LiveRow("CPU freq", CpuReader.formatFreq(liveCpuFreq))
                    }
                }
            }

            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = "Error",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.danger
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = error ?: "",
                            fontSize = 13.sp,
                            color = colors.onSurface
                        )
                    }
                }
            }

            if (!isRunning && result == null) {
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isRunning = true
                                error = null
                                progress = 0

                                val startBatteryTemp = BatteryReader.temperature(context)
                                val startCpuTemp = CpuReader.temperature()

                                currentStep = "AES single-core"
                                val aesSingle = AesBench.runSingleCore { p -> progress = p / 4 }
                                val aesSingleEnd = AesBench.runSingleCore { }

                                currentStep = "AES multi-core"
                                val aesMulti = AesBench.runMultiCore { p ->
                                    progress = 25 + p / 4
                                }

                                currentStep = "Storage"
                                val stResult = StorageBench.run(context) { p ->
                                    progress = 50 + p / 2
                                }

                                val endCpuTemp = CpuReader.temperature()
                                val endBatteryTemp = BatteryReader.temperature(context)

                                currentStep = "Calculating…"
                                progress = 95

                                val throttling = Scoring.calculateThrottling(aesSingle, aesSingleEnd)

                                val finalResult = Scoring.calculate(
                                    aesSingleMbPerSec = aesSingle,
                                    aesMultiMbPerSec = aesMulti,
                                    storageWriteMbPerSec = stResult.writeMbPerSec,
                                    storageReadMbPerSec = stResult.readMbPerSec,
                                    storageRandomMbPerSec = stResult.randomReadMbPerSec,
                                    throttlingPercent = throttling
                                ).copy(
                                    cpuTempStart = startCpuTemp,
                                    cpuTempEnd = endCpuTemp,
                                    batteryTempStart = startBatteryTemp,
                                    batteryTempEnd = endBatteryTemp,
                                    deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL,
                                    deviceModel = android.os.Build.MODEL
                                )

                                result = finalResult

                                try {
                                    Preferences.saveResult(context, finalResult)
                                    Log.i("Metrik", "Результат сохранён в историю")
                                } catch (e: Exception) {
                                    Log.e("Metrik", "Не удалось сохранить результат", e)
                                }

                                progress = 100
                                currentStep = "Done"
                                isRunning = false
                            } catch (e: Exception) {
                                Log.e("Metrik", "Ошибка бенчмарка", e)
                                error = e.message ?: "Unknown error"
                                isRunning = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.background
                    )
                ) {
                    Text("START BENCHMARK", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (result != null) {
                Spacer(Modifier.height(20.dp))
                ResultBlock(result!!)

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { result = null; progress = 0 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.background
                    )
                ) {
                    Text("REPEAT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LiveRow(label: String, value: String) {
    val colors = LocalMetrikColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = colors.onSurfaceVariant)
        Text(value, fontSize = 12.sp, color = colors.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ResultBlock(result: BenchResult) {
    val colors = LocalMetrikColors.current

    // ===== HERO: итоговый балл =====
    BentoCard(
        title = "Result",
        value = Scoring.formatScore(result.totalScore),
        subtitle = "/ ${Scoring.formatScore(Scoring.MAX_TOTAL)} · ${result.rating}",
        accentValue = true,
        valueSize = 44,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    // ===== CPU =====
    BentoCardWithRows(
        title = "CPU",
        value = Scoring.formatScore(result.cpuScore),
        rows = listOf(
            "AES single" to String.format("%.1f MB/s", result.aesSingleMbPerSec),
            "AES multi" to String.format("%.1f MB/s", result.aesMultiMbPerSec)
        ),
        subtitle = "/ ${Scoring.formatScore(Scoring.MAX_CPU)}",
        icon = Icons.Default.Memory,
        accentValue = false,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    // ===== Storage =====
    BentoCardWithRows(
        title = "Storage",
        value = Scoring.formatScore(result.storageScore),
        rows = listOf(
            "Write" to String.format("%.0f MB/s", result.storageWriteMbPerSec),
            "Read" to String.format("%.0f MB/s", result.storageReadMbPerSec),
            "Random 4K" to String.format("%.0f MB/s", result.storageRandomMbPerSec)
        ),
        subtitle = "/ ${Scoring.formatScore(Scoring.MAX_STORAGE)}",
        icon = Icons.Default.SdStorage,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    // ===== Battery =====
    BentoCardWithRows(
        title = "Battery",
        value = Scoring.formatScore(result.batteryScore),
        rows = listOf(
            "Throttling" to String.format("%.1f%%", result.throttlingPercent),
            "CPU start" to String.format("%.1f°C", result.cpuTempStart),
            "CPU end" to String.format("%.1f°C", result.cpuTempEnd),
            "Batt start" to String.format("%.1f°C", result.batteryTempStart),
            "Batt end" to String.format("%.1f°C", result.batteryTempEnd)
        ),
        subtitle = "/ ${Scoring.formatScore(Scoring.MAX_BATTERY)}",
        icon = Icons.Default.BatteryFull,
        modifier = Modifier.fillMaxWidth()
    )
}
