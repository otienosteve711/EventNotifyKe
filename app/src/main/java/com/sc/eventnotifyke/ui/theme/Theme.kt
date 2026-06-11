package com.sc.eventnotifyke.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = CrimsonPrimary,
    onPrimary            = Color.White,
    primaryContainer     = CrimsonSurface,
    onPrimaryContainer   = CrimsonPrimary,
    secondary            = NavyDeep,
    onSecondary          = Color.White,
    secondaryContainer   = NavyMid,
    onSecondaryContainer = IceWhite,
    tertiary             = AmberAccent,
    onTertiary           = Color.White,
    tertiaryContainer    = AmberSurface,
    onTertiaryContainer  = AmberAccent,
    background           = IceWhite,
    onBackground         = NavyDeep,
    surface              = CardLight,
    onSurface            = NavyDeep,
    onSurfaceVariant     = NavyText,
    outline              = Color(0xFFE0E0E6)
)

private val DarkColorScheme = darkColorScheme(
    primary              = CrimsonDark,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFF3A0F1A),
    onPrimaryContainer   = CrimsonDark,
    secondary            = Color(0xFFC8C8D8),
    onSecondary          = NavyDark,
    secondaryContainer   = NavyMid,
    onSecondaryContainer = Color(0xFFE8E8F0),
    tertiary             = AmberDark,
    onTertiary           = NavyDark,
    tertiaryContainer    = Color(0xFF3A2010),
    onTertiaryContainer  = AmberDark,
    background           = NavyDark,
    onBackground         = Color(0xFFE8E8F0),
    surface              = CardDark,
    onSurface            = Color(0xFFE8E8F0),
    onSurfaceVariant     = SlateText,
    outline              = Color(0xFF2A2A45)
)

@Composable
fun EventNotifyKETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}