package com.monish.insight.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.monish.insight.ui.home.HomeScreen
import com.monish.insight.ui.bookmarks.BookmarksScreen
import com.monish.insight.ui.profile.ProfileScreen
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding


@Composable
fun BottomNavGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") { HomeScreen() }
        composable("bookmarks") { BookmarksScreen() }
        composable("profile") {
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }
    }
}
