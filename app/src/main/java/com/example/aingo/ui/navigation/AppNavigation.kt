package com.example.aingo.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.aingo.ui.chat.ChatScreen
import com.example.aingo.ui.screens.FlashcardsScreen
import com.example.aingo.ui.screens.ProfileScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Стан клавіатури
    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0C1825))) {
        // Фон
        Canvas(modifier = Modifier.fillMaxSize().blur(100.dp)) {
            drawCircle(Color(0xFF00E5FF).copy(0.12f), 500f, Offset(size.width, 0f))
            drawCircle(Color(0xFFFF3D00).copy(0.08f), 700f, Offset(0f, size.height))
        }

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = if (isKeyboardVisible) {
                WindowInsets.statusBars
            } else {
                WindowInsets.systemBars
            },
            bottomBar = {
                if (currentRoute != "auth" && currentRoute != null && !isKeyboardVisible) {
                    Box(modifier = Modifier
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                    ) {
                        StaticGlassyNavigation(navController = navController)
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Chat.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = if (isKeyboardVisible) 0.dp else innerPadding.calculateBottomPadding()
                    )
            ) {
                composable(Screen.Chat.route) { ChatScreen() }
                composable(Screen.Flashcards.route) { FlashcardsScreen() }
                composable(Screen.Profile.route) {
                    ProfileScreen(onNavigateToLogin = {
                        navController.navigate("auth") { popUpTo(0) }
                    })
                }
            }
        }
    }
}
@Composable
fun StaticGlassyNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(35.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavigationItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.3f),
                    label = "iconColor"
                )

                Column(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            if (!isSelected) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(screen.icon, null, tint = iconColor, modifier = Modifier.size(26.dp))
                    if (isSelected) {
                        Box(Modifier.padding(top = 4.dp).size(4.dp).background(Color(0xFF00E5FF), CircleShape))
                    }
                }
            }
        }
    }
}