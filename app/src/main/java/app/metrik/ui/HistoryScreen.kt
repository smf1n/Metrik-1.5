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
