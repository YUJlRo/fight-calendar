package com.fightcalendar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fightcalendar.app.ui.screens.DayScreen
import com.fightcalendar.app.ui.screens.OnboardingScreen
import com.fightcalendar.app.ui.theme.FightCalendarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FightCalendarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FightCalendarApp()
                }
            }
        }
    }
}

@Composable
fun FightCalendarApp() {
    val navController = rememberNavController()
    var isFirstLaunch by remember { mutableStateOf(true) } // TODO: Load from preferences
    
    NavHost(
        navController = navController,
        startDestination = if (isFirstLaunch) "onboarding" else "day"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    isFirstLaunch = false
                    navController.navigate("day") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("day") {
            DayScreen(
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToCategories = {
                    navController.navigate("categories")
                }
            )
        }
        
        composable("settings") {
            // TODO: Implement settings screen
            DayScreen(
                onNavigateToSettings = { navController.popBackStack() }
            )
        }
        
        composable("categories") {
            // TODO: Implement category management screen
            DayScreen(
                onNavigateToSettings = { navController.popBackStack() }
            )
        }
    }
}