package com.monish.insight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.monish.insight.ui.navigation.BottomBar
import com.monish.insight.ui.navigation.BottomNavGraph
import com.monish.insight.ui.theme.InSightTheme
import com.monish.insight.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = true)

            InSightTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomBar(navController = navController) }
                ) { innerPadding ->
                    BottomNavGraph(
                        navController = navController,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { enabled ->
                            themeViewModel.toggleTheme(enabled)
                        },
                        modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
