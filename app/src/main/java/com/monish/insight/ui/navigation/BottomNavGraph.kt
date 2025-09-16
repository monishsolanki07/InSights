package com.monish.insight.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.monish.insight.ui.home.HomeScreen
import com.monish.insight.ui.home.ArticleReelsScreen

import com.monish.insight.ui.bookmarks.BookmarksScreen
import com.monish.insight.ui.profile.ProfileScreen
import androidx.compose.ui.Modifier
import com.monish.insight.ui.home.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel




@Composable
fun BottomNavGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home", // default start destination
        modifier = modifier
    ) {

        /*
        User taps BottomBarItem("articles") → navController.navigate("articles").
        NavController looks into NavHost
        → finds composable("articles")
        → shows ArticleReelsScreen().
         */
        composable("home") { HomeScreen() }
        composable("articles") { ArticleReelsScreen() }
        composable("bookmarks") { BookmarksScreen() }
        composable("profile") {
            val homeViewModel: HomeViewModel = viewModel()
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                homeViewModel = homeViewModel
            )
        }

    }
}
