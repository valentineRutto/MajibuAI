package com.valentinerutto.majibuai.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.valentinerutto.majibuai.ui.MajibuViewModel
import com.valentinerutto.majibuai.ui.ask.AskScreen
import com.valentinerutto.majibuai.ui.library.LibraryScreen

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

        val viewModel: MajibuViewModel = koinViewModel()

        NavHost(
            navController = navController,
            startDestination = "root",
            modifier = Modifier.padding(innerPadding),
        ) {
            navigation(startDestination = MajibuRoute.Ask.route, route = "root") {

                composable(MajibuRoute.Ask.route) { backStackEntry ->

                    AskScreen(viewModel = viewModel)
                }
                composable(MajibuRoute.Library.route) { backStackEntry ->

                   LibraryScreen(
                        savedQuestions = viewModel.savedQuestions.collectAsStateWithLifecycle().value,
                    )

                }
                composable(MajibuRoute.Settings.route) {
                    PlaceholderScreen(title = "Settings")
                }
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
