package com.example.endoquest.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryLight,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF004D40),
    secondary = SecondaryGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF5D4037),
    tertiary = PrimaryBlue,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    error = AccentCoral,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    secondary = SecondaryGold,
    tertiary = PrimaryBlue
)

@Composable
fun EndoQuestTheme(
    darkTheme: Boolean = false, // Force vibrant light theme for lively dental look
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
