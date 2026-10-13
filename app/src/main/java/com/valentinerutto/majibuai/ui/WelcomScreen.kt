package com.valentinerutto.majibuai.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinerutto.majibuai.R
import com.valentinerutto.majibuai.ui.theme.AppBackground
import com.valentinerutto.majibuai.ui.theme.MintPrimary
import com.valentinerutto.majibuai.ui.theme.TextPrimary
import com.valentinerutto.majibuai.ui.theme.TextSecondary
import com.valentinerutto.majibuai.ui.theme.YellowAccent

@Composable
fun WelcomScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(AppBackground)) {
        WelcomeDecoration(
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.height(12.dp))

            Image(
                painter = painterResource(R.drawable.majibu_robot),
                contentDescription = "MajibuAI study assistant mascot",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp),
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Majibu",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-1).sp,
                    )
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MintPrimary,
                        letterSpacing = (-1).sp,
                    )
                }
                Text(
                    text = "Ask. Learn. Grow.",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Text(
                text = "Your AI study buddy for\ntech, conferences and beyond.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 25.sp,
            )

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(58.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintPrimary,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.size(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun WelcomeDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = MintPrimary.copy(alpha = 0.78f),
            radius = size.width * 0.27f,
            center = Offset(-size.width * 0.1f, -size.height * 0.02f),
        )
        drawCircle(
            color = MintPrimary.copy(alpha = 0.9f),
            radius = size.width * 0.11f,
            center = Offset(size.width * 1.05f, size.height * 0.43f),
        )
        drawCircle(
            color = MintPrimary.copy(alpha = 0.8f),
            radius = size.width * 0.18f,
            center = Offset(-size.width * 0.08f, size.height * 1.02f),
        )
        drawCircle(
            color = YellowAccent.copy(alpha = 0.95f),
            radius = size.width * 0.23f,
            center = Offset(size.width * 1.03f, size.height * 1.04f),
        )
    }
}
