package com.valentinerutto.majibuai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.valentinerutto.majibuai.navigation.MajibuNavGraph
import com.valentinerutto.majibuai.ui.theme.MajibuAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MajibuAITheme {
                MajibuNavGraph()
            }
        }
    }
}
