package app.metrik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

    // Живые данные во время теста
    var liveCpuTemp by remember { mutableStateOf(0f) }
    var liveBatteryTemp by remember { mutableStateOf(0f) }
    var liveCpuFreq by remember { mutableStateOf(0L) }

    // Обновление живых данных
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

            // ===== Прогресс =====
            if (isRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = currentStep,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )

                        Spacer(Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            color = colors.accent,
                            trackColor = colors.divider
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "$progress%",
                            fontSize = 14.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Живые датчики
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = "🌡 Live",
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

            // ===== Ошибка =====
            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = "❌ Error",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
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

            // ===== Кнопка НАЧАТЬ =====
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

                                // === AES SINGLE ===
                                currentStep = "AES single-core"
                                val aesSingle = AesBench.runSingleCore { p -> progress = p / 4 }
                                val aesSingleEnd = AesBench.runSingleCore { }  // второй замер для троттлинга

                                // === AES MULTI ===
                                currentStep = "AES multi-core"
                                val aesMulti = AesBench.runMultiCore { p ->
                                    progress = 25 + p / 4
                                }

                                // === STORAGE ===
                                currentStep = "Storage"
                                val stResult = StorageBench.run(context) { p ->
                                    progress = 50 + p / 2
                                }

                                val endCpuTemp = CpuReader.temperature()
                                val endBatteryTemp = BatteryReader.temperature(context)

                                // === ПОДСЧЁТ ===
                                currentStep = "Calculating…"
                                progress = 95

                                val throttling = Scoring.calculateThrottling(aesSingle, aesSingleEnd)

                                result = Scoring.calculate(
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

                                progress = 100
                                currentStep = "Done"
                                isRunning = false
                            } catch (e: Exception) {
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
                    Text("🚀 START BENCHMARK", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ===== РЕЗУЛЬТАТ =====
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
                    Text("🔄 REPEAT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

    // === Итоговый балл ===
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 RESULT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "${result.totalScore}",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )

            Text(
                text = "/ ${Scoring.MAX_TOTAL}",
                fontSize = 16.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = result.rating,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = result.deviceName,
                fontSize = 12.sp,
                color = colors.onSurfaceVariant
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    // === CPU ===
    CategoryCard(
        title = "🔲 CPU",
        score = result.cpuScore,
        max = 2000,
        rows = listOf(
            "AES single" to String.format("%.1f МБ/с", result.aesSingleMbPerSec),
            "AES multi" to String.format("%.1f МБ/с", result.aesMultiMbPerSec)
        )
    )

    Spacer(Modifier.height(12.dp))

    // === Storage ===
    CategoryCard(
        title = "💿 Storage",
        score = result.storageScore,
        max = 3000,
        rows = listOf(
            "Write" to String.format("%.0f МБ/с", result.storageWriteMbPerSec),
            "Read" to String.format("%.0f МБ/с", result.storageReadMbPerSec),
            "Random 4K" to String.format("%.0f МБ/с", result.storageRandomMbPerSec)
        )
    )

    Spacer(Modifier.height(12.dp))

    // === Battery ===
    CategoryCard(
        title = "🔋 Battery",
        score = result.batteryScore,
        max = 1000,
        rows = listOf(
            "Throttling" to String.format("%.1f%%", result.throttlingPercent),
            "CPU temp start" to String.format("%.1f°C", result.cpuTempStart),
            "CPU temp end" to String.format("%.1f°C", result.cpuTempEnd),
            "Batt temp start" to String.format("%.1f°C", result.batteryTempStart),
            "Batt temp end" to String.format("%.1f°C", result.batteryTempEnd)
        )
    )
}

@Composable
private fun CategoryCard(
    title: String,
    score: Int,
    max: Int,
    rows: List<Pair<String, String>>
) {
    val colors = LocalMetrikColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )

                Text(
                    text = "$score / $max  ${Scoring.categoryEmoji(score, max)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Spacer(Modifier.height(12.dp))

            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, fontSize = 12.sp, color = colors.onSurfaceVariant)
                    Text(value, fontSize = 12.sp, color = colors.onSurface, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
