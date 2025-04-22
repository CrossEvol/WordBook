package com.crossevol.wordbook

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.crossevol.wordbook.data.model.WordItem
import com.crossevol.wordbook.ui.components.sampleWordItem
import com.crossevol.wordbook.ui.screens.HomePage
import com.crossevol.wordbook.ui.screens.SettingsPage // Import the new Settings Page
import com.crossevol.wordbook.ui.screens.WordDetailPage
import com.crossevol.wordbook.ui.screens.WordReviewPage
import org.jetbrains.compose.ui.tooling.preview.Preview

// Define screen states for navigation
sealed class Screen {
    object Home : Screen()
    data class Detail(val word: WordItem) : Screen()
    data class Review(val word: WordItem) : Screen()
    object Settings : Screen() // Add Settings screen state
}

@Composable
@Preview
fun App() {
    // Navigation state using the sealed class
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    MaterialTheme {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomePage(
                    onWordItemClick = { word ->
                        currentScreen = Screen.Detail(word) // Navigate to Detail
                    },
                    onNavigate = { route ->
                        println("Bottom nav clicked: $route")
                        when (route) {
                            "home" -> currentScreen = Screen.Home // Stay home or return home
                            "review" -> currentScreen = Screen.Review(sampleWordItem) // Navigate to Review
                            "settings" -> currentScreen = Screen.Settings // Navigate to Settings
                        }
                    }
                )
            }
            is Screen.Detail -> {
                WordDetailPage(
                    wordItem = screen.word,
                    onBack = {
                        currentScreen = Screen.Home // Navigate back to Home from Detail
                    }
                )
            }
            is Screen.Review -> {
                WordReviewPage(
                    wordItem = screen.word, // Use the word passed in the state (currently sampleWordItem)
                    onAction = {
                        // Action taken (Edit, Delete, Cancel), dismiss overlay and go back home
                        currentScreen = Screen.Home
                        println("Review action taken, returning home.")
                    },
                    onBack = {
                        // Explicit back navigation from Review screen, go back home
                        currentScreen = Screen.Home
                        println("Review back pressed, returning home.")
                    }
                )
            }
            is Screen.Settings -> {
                SettingsPage(
                    onNavigateBack = { currentScreen = Screen.Home }, // Navigate back to Home
                    // Add other callbacks as needed, e.g.:
                    onLogout = {
                        println("Logout clicked!")
                        // Implement actual logout logic and navigate (e.g., to a login screen or back home)
                        currentScreen = Screen.Home // Example: Go back home after logout
                    },
                    onEditProfile = { println("Edit Profile clicked!") },
                    onChangeApiKey = { println("Change API Key clicked!") },
                    onNotificationSettings = { println("Notification Settings clicked!") },
                    onIntroduction = { println("Introduction clicked!") },
                    onTermsOfService = { println("Terms of Service clicked!") }
                )
            }
        }
    }
}
