package com.valentinerutto.majibuai.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.valentinerutto.majibuai.ui.ask.AskScreen

sealed class MajibuRoute(val route: String) {
    data object Ask : MajibuRoute("ask")
    data object Library : MajibuRoute("library")
    data object Settings : MajibuRoute("settings")
}

@Composable
fun MajibuNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { MajibuBottomBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MajibuRoute.Ask.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(MajibuRoute.Ask.route) { AskScreen() }
            composable(MajibuRoute.Library.route) {
                PlaceholderScreen(title = "Library")
            }
            composable(MajibuRoute.Settings.route) {
                PlaceholderScreen(title = "Settings")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = title)
    }
}
