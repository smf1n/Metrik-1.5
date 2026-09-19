package app.metrik.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.metrik.bench.BenchResult
import app.metrik.bench.Scoring
import app.metrik.util.Preferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onRunBench: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = LocalMetrikColors.current

    var history by remember { mutableStateOf(Preferences.loadHistory(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "History",
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
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = {
                            Preferences.clearHistory(context)
                            history = emptyList()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear",
                                tint = colors.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(72.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "No results yet",
                        fontSize = 18.sp,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Run a benchmark to see it here",
                        fontSize = 13.sp,
                        color = colors.onSurfaceVariant
                    )

                    if (onRunBench != null) {
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onRunBench,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.background
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Run Benchmark", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { item ->
                    HistoryItem(item)
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(result: BenchResult) {
    val colors = LocalMetrikColors.current
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                Text(
                    text = Scoring.formatScore(result.totalScore),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )

                Text(
                    text = result.rating,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = result.deviceName.ifEmpty { "Unknown Device" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = dateFormat.format(Date(result.timestamp)),
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}
