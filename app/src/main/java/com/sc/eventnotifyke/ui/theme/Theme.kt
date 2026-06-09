package com.sc.eventnotifyke.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryTeal,
    secondary = BrandPrimaryTeal,
    tertiary = Pink80,
    primaryContainer = BrandPrimaryTeal.copy(alpha = 0.2f),
    onPrimaryContainer = BrandPrimaryTeal,
    onSurfaceVariant = BrandPrimaryTeal
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimaryTeal,            // Active borders, primary button backgrounds
    onPrimary = Color.White,               // Button text colors
    secondary = BrandPrimaryTeal,          // Replaces PurpleGrey40 so links are Teal!
    primaryContainer = BrandPrimaryTeal.copy(alpha = 0.15f), // Zone chip backgrounds
    onPrimaryContainer = BrandPrimaryTeal, // Zone chip active text highlights
    onSurfaceVariant = BrandPrimaryTeal,   // Focused vector graphics input field icons
    tertiary = Pink40
)

@Composable
fun EventNotifyKETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color block removed completely to enforce your custom identity 100% of the time
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}