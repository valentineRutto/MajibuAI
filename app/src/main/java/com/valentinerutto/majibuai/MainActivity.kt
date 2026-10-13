package com.valentinerutto.majibuai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.valentinerutto.majibuai.ui.WelcomScreen
import com.valentinerutto.majibuai.navigation.MajibuNavGraph
import com.valentinerutto.majibuai.ui.theme.AppBackground
import com.valentinerutto.majibuai.ui.theme.MajibuAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MajibuAITheme {
                var hasCompletedWelcome by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    hasCompletedWelcome = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
                        .getBoolean(KEY_WELCOME_COMPLETED, false)
                }

                when (hasCompletedWelcome) {
                    null -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppBackground),
                    )
                    false -> WelcomScreen(
                        onGetStarted = {
                            getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
                                .edit()
                                .putBoolean(KEY_WELCOME_COMPLETED, true)
                                .apply()
                            hasCompletedWelcome = true
                        },
                    )
                    true -> MajibuNavGraph()
                }
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "majibu_ai_preferences"
        const val KEY_WELCOME_COMPLETED = "welcome_completed"
    }
}
