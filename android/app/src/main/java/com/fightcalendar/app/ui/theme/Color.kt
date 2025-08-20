package com.fightcalendar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Theme Colors
val LightPrimary = Color(0xFF81CAC4)
val LightOnPrimary = Color(0xFF063A3A)
val LightPrimaryContainer = Color(0xFFBAEFEB)
val LightOnPrimaryContainer = Color(0xFF073433)
val LightSecondary = Color(0xFF0F3B46)
val LightOnSecondary = Color(0xFFE0F7F5)
val LightTertiary = Color(0xFFFF7A59)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF7FBFB)
val LightOnBackground = Color(0xFF063A3A)
val LightOnSurface = Color(0xFF063A3A)

// Dark Theme Colors
val DarkPrimary = Color(0xFF5DB5AF)
val DarkOnPrimary = Color(0xFF041E1E)
val DarkPrimaryContainer = Color(0xFF0E3432)
val DarkOnPrimaryContainer = Color(0xFFC7F6F1)
val DarkSecondary = Color(0xFF0B2C33)
val DarkOnSecondary = Color(0xFFD5F2EF)
val DarkTertiary = Color(0xFFFF8A6B)
val DarkOnTertiary = Color(0xFF1F0E0B)
val DarkBackground = Color(0xFF041E1E)
val DarkSurface = Color(0xFF0E1818)
val DarkOnBackground = Color(0xFFC7F6F1)
val DarkOnSurface = Color(0xFFC7F6F1)

// Fixed Category Colors
val CategoryWork = Color(0xFF2BB673)
val CategoryStudy = Color(0xFF2F6DF6)
val CategoryEntertainment = Color(0xFFFF7A59)
val CategoryTools = Color(0xFF7E57C2)
val CategoryUnusedLight = Color(0xFFDFECEA)
val CategoryUnusedDark = Color(0xFF2A3A39)

// Widget & Overlay Colors
val WidgetEventOverlay = Color(0x3481CAC4) // 20% transparent
val FreeTimeHighlight = Color(0x1F81CAC4) // 12% transparent

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface
)

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
)

// Utility functions
@Composable
fun getCategoryColor(categoryId: String?): Color {
    return when (categoryId) {
        "work" -> CategoryWork
        "study" -> CategoryStudy
        "entertainment" -> CategoryEntertainment
        "tools" -> CategoryTools
        else -> getUnusedColor()
    }
}

@Composable
fun getUnusedColor(): Color {
    return if (androidx.compose.foundation.isSystemInDarkTheme()) {
        CategoryUnusedDark
    } else {
        CategoryUnusedLight
    }
}