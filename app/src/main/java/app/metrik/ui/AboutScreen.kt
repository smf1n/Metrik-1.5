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
import app.metrik.bench.Scoring

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
                text = "Version 1.5.1",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            // ===== What is this =====
            AboutCard(title = "What is this") {
                Text(
                    text = "Metrik is a fair Android benchmark. " +
                            "It shows real physical values " +
                            "(encryption speed, disk speed, temperatures) " +
                            "and calculates a total score out of ${Scoring.formatScore(Scoring.MAX_TOTAL)}.",
                    fontSize = 13.sp,
                    color = colors.onSurface,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // ===== Scoring =====
            AboutCard(title = "How scores work") {
                ScoreInfoRow("AES single", "max 1 000 000", "1000 MB/s = 1 000 000")
                ScoreInfoRow("AES multi", "max 1 000 000", "4000 MB/s = 1 000 000")
                ScoreInfoRow("Storage write", "max 1 000 000", "1500 MB/s = 1 000 000")
                ScoreInfoRow("Storage read", "max 1 000 000", "2500 MB/s = 1 000 000")
                ScoreInfoRow("Storage random", "max 1 000 000", "500 MB/s = 1 000 000")
                ScoreInfoRow("Throttling", "max 1 000 000", "0% = 1 000 000, -50% = 0")

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Maximum — ${Scoring.formatScore(Scoring.MAX_TOTAL)} points. " +
                            "Scores are based on physical constants, not on a specific device.",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // ===== Ratings =====
            AboutCard(title = "Ratings") {
                ScoreInfoRow("EXCELLENT", "6 000 000+", "")
                ScoreInfoRow("VERY GOOD", "4 500 000+", "")
                ScoreInfoRow("GOOD", "3 000 000+", "")
                ScoreInfoRow("AVERAGE", "1 800 000+", "")
                ScoreInfoRow("LOW", "800 000+", "")
                ScoreInfoRow("VERY LOW", "0+", "")
            }

            Spacer(Modifier.height(12.dp))

            // ===== Tech =====
            AboutCard(title = "Technologies") {
                Text(
                    text = "• Kotlin 1.9\n" +
                            "• Jetpack Compose\n" +
                            "• AES-256 encryption\n" +
                            "• Real disk measurements\n" +
                            "• No ads\n" +
                            "• Open source",
                    fontSize = 13.sp,
                    color = colors.onSurface,
                    lineHeight = 22.sp
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Made with ♥ in 2026",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AboutCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = LocalMetrikColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )

            Spacer(Modifier.height(12.dp))

            content()
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
