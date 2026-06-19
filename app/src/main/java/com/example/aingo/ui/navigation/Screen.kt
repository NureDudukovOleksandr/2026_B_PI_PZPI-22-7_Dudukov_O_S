package com.example.aingo.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Style
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Chat : Screen("chat", "Chat", Icons.Rounded.ChatBubbleOutline)
    object Flashcards : Screen("cards", "Cards", Icons.Rounded.Style)
    object Profile : Screen("profile", "Profile", Icons.Rounded.PersonOutline)
}

val bottomNavigationItems = listOf(
    Screen.Chat,
    Screen.Flashcards,
    Screen.Profile
)