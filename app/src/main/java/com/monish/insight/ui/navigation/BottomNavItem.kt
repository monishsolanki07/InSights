package com.monish.insight.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector


/*
we use data class because we can easily compare to values
its not possbile in kotlin to compare it direclty
if we create two obj of normal class we cant compare them

We need a data class when:
You want a class whose main purpose is to store data.
You want clean, readable code without writing boilerplate.
You want automatic equals(), hashCode(), toString(), copy(), componentN().
 */
data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)