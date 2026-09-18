package app.metrik.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val colors = LocalMetrikColors.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About",
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // ===== Логотип =====
            Text(
                text = "Metrik",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )

            Text(
                text = "Phone Benchmark",
                fontSize = 14.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Version 1.5.0",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            // ===== Описание =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "📊 Что это",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Metrik — это честный бенчмарк для Android. " +
                                "Он показывает реальные физические значения " +
                                "(скорость шифрования, скорость диска, температуры) " +
                                "и рассчитывает итоговый балл из 7000.",
                        fontSize = 13.sp,
                        color = colors.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ===== Как считаются баллы =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "🧮 Как считаются баллы",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )

                    Spacer(Modifier.height(12.dp))

                    ScoreInfoRow("AES single", "макс 1000", "1000 МБ/с = 1000")
                    ScoreInfoRow("AES multi", "макс 1000", "4000 МБ/с = 1000")
                    ScoreInfoRow("Storage write", "макс 1000", "1500 МБ/с = 1000")
                    ScoreInfoRow("Storage read", "макс 1000", "2500 МБ/с = 1000")
                    ScoreInfoRow("Storage random", "макс 1000", "500 МБ/с = 1000")
                    ScoreInfoRow("Throttling", "макс 1000", "0% = 1000, -50% = 0")

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Максимум — 7000 баллов. Баллы не привязаны к " +
                                "конкретному устройству, а используют физические константы.",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ===== Рейтинги =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "🏆 Рейтинги",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )

                    Spacer(Modifier.height(12.dp))

                    ScoreInfoRow("⭐ Excellent", "6000+", "")
                    ScoreInfoRow("✅ Very Good", "4500+", "")
                    ScoreInfoRow("👍 Good", "3000+", "")
                    ScoreInfoRow("🟡 Average", "1800+", "")
                    ScoreInfoRow("🟠 Low", "800+", "")
                    ScoreInfoRow("🔴 Very Low", "0+", "")
                }
            }

            Spacer(Modifier.height(12.dp))

            // ===== Технологии =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "🛠 Технологии",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "• Kotlin 1.9\n" +
                                "• Jetpack Compose\n" +
                                "• AES-256 шифрование\n" +
                                "• Реальные замеры диска\n" +
                                "• Без рекламы\n" +
                                "• Открытый исходный код",
                        fontSize = 13.sp,
                        color = colors.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // ===== Подпись =====
            Text(
                text = "Made with ❤️ in 2025",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ScoreInfoRow(label: String, value: String, hint: String) {
    val colors = LocalMetrikColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = colors.onSurface,
                fontWeight = FontWeight.Medium
            )
            if (hint.isNotEmpty()) {
                Text(
                    text = hint,
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant
                )
            }
        }

        Text(
            text = value,
            fontSize = 12.sp,
            color = colors.accent,
            fontWeight = FontWeight.Bold
        )
    }
}
