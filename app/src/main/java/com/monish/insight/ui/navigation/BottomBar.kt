package com.monish.insight.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Description


@Composable
fun BottomBar(navController: NavController) {
    // for adding icons in bottom nav
    val items = listOf(
        BottomNavItem("Home", "home", Icons.Filled.Home),
        BottomNavItem("Articles", "articles", Icons.Filled.Description),
        BottomNavItem("Bookmarks", "bookmarks", Icons.Filled.Bookmark),
        BottomNavItem("Profile", "profile", Icons.Filled.Person)
    )

    NavigationBar {
        /*
            USING STACK :
             -> a stack to store values , in the order previously visited screeen
         */
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true  // no duplicate screen
                        restoreState = true  //restores previous UI state (scroll position, input)
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.name) },
                label = { Text(item.name) }
            )
        }
    }
}
