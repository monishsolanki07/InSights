package com.monish.insight.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.monish.insight.ui.home.HomeScreen
import com.monish.insight.ui.home.ArticleReelsScreen
import com.monish.insight.ui.reels.ReelScreen

import com.monish.insight.ui.bookmarks.BookmarksScreen
import com.monish.insight.ui.profile.ProfileScreen
import androidx.compose.ui.Modifier
import com.monish.insight.ui.home.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monish.insight.ui.bookmarks.BookmarksViewModel

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Single instance of BookmarksViewModel
    val bookmarksViewModel: BookmarksViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                homeViewModel = homeViewModel,
                bookmarksViewModel = bookmarksViewModel
            )
        }
        composable("articles") {
            val homeViewModel: HomeViewModel = viewModel()
            ArticleReelsScreen(
                homeViewModel = homeViewModel,
                bookmarksViewModel = bookmarksViewModel
            )
        }
        composable("bookmarks") {
            BookmarksScreen(viewModel = bookmarksViewModel)
        }
        composable("profile") {
            val homeViewModel: HomeViewModel = viewModel()
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                homeViewModel = homeViewModel
            )
        }
        composable("reels") {
            ReelScreen(bookmarksViewModel = bookmarksViewModel)
        }


    }
}
