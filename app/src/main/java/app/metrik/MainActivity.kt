package app.metrik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import app.metrik.ui.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current

            var currentTheme by remember { mutableStateOf(ThemeChoice.current) }

            MetrikTheme(theme = currentTheme) {
                val colors = LocalMetrikColors.current

                var screen by remember { mutableStateOf("home") }
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = colors.background
                        ) {
                            DrawerContent(
                                onNavigate = { route ->
                                    screen = route
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = colors.background
                    ) {
                        when (screen) {
                            "home" -> MainScreen(
                                onOpenDrawer = {
                                    scope.launch { drawerState.open() }
                                },
                                onStartBench = {
                                    screen = "bench"
                                }
                            )

                            "bench" -> BenchScreen(
                                onBack = { screen = "home" }
                            )

                            "history" -> HistoryScreen(
                                onBack = { screen = "home" },
                                onRunBench = { screen = "bench" }
                            )

                            "settings" -> SettingsScreen(
                                onBack = {
                                    currentTheme = ThemeChoice.current
                                    screen = "home"
                                }
                            )

                            "about" -> AboutScreen(
                                onBack = { screen = "home" }
                            )

                            else -> MainScreen(
                                onOpenDrawer = {
                                    scope.launch { drawerState.open() }
                                },
                                onStartBench = {
                                    screen = "bench"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
