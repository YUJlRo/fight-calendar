package com.fightcalendar.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun FightCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color for brand consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Get category color based on category type
 */
@Composable
fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
    return when (category.lowercase()) {
        "work" -> CategoryWork
        "study" -> CategoryStudy
        "entertainment" -> CategoryEntertainment
        "tools" -> CategoryTools
        else -> if (isSystemInDarkTheme()) CategoryUnusedDark else CategoryUnusedLight
    }
}

/**
 * Get unused/unclassified color based on theme
 */
@Composable
fun getUnusedColor(): androidx.compose.ui.graphics.Color {
    return if (isSystemInDarkTheme()) CategoryUnusedDark else CategoryUnusedLight
}