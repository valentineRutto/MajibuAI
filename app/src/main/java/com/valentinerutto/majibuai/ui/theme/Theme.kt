package com.valentinerutto.majibuai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.valentinerutto.majibuai.R
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = MintLight,
    onPrimaryContainer = MintDark,

    secondary = LavenderAccent,
    onSecondary = SurfaceWhite,
    secondaryContainer = LavenderLight,
    onSecondaryContainer = TextPrimary,

    tertiary = PeachAccent,
    onTertiary = TextPrimary,
    tertiaryContainer = PeachLight,
    onTertiaryContainer = TextPrimary,

    background = AppBackground,
    onBackground = TextPrimary,

    surface = SurfaceWhite,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = TextSecondary,

    surfaceContainer = SurfaceSoft,
    surfaceContainerLow = SurfaceWhite,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerHigh = SurfaceSoft,
    surfaceContainerHighest = SurfaceSoft,

    outline = BorderLight,
    outlineVariant = BorderLight,

    error = ErrorRed,
    onError = SurfaceWhite
)



val Nunito = FontFamily(
    Font(R.font.nunitoregular, FontWeight.Normal),
    Font(R.font.nunitomedium, FontWeight.Medium),
    Font(R.font.nunitosemibold, FontWeight.SemiBold),
    Font(R.font.nunitobold, FontWeight.Bold),
    Font(R.font.nunitoextrabold, FontWeight.ExtraBold)
)

val MajibuTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun MajibuAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MajibuTypography,
        shapes = MajibuShapes,
        content = content
    )
}