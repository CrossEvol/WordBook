package com.crossevol.wordbook

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import com.crossevol.wordbook.data.model.WordItem
import com.crossevol.wordbook.ui.components.sampleWordItem
import com.crossevol.wordbook.ui.screens.ApiKeySettingPage // Import ApiKeySettingPage
import com.crossevol.wordbook.ui.screens.EditProfilePage // Import EditProfilePage
import com.crossevol.wordbook.ui.screens.HomePage
import com.crossevol.wordbook.ui.screens.SettingsPage
import com.crossevol.wordbook.ui.screens.WordDetailPage
import com.crossevol.wordbook.ui.screens.WordFetchPage
import com.crossevol.wordbook.ui.screens.WordReviewPage
import org.jetbrains.compose.ui.tooling.preview.Preview

// Define screen states for navigation
sealed class Screen {
    object Home : Screen()
    data class Detail(val word: WordItem) : Screen()
    data class Review(val word: WordItem) : Screen()
    object Settings : Screen()
    object EditProfile : Screen() // Add EditProfile screen state
    object ApiKeySettings : Screen() // Add ApiKeySettings screen state
    object WordFetch : Screen() // Add WordFetch screen state
}

@Composable
@Preview
fun App() {
    // Navigation state using the sealed class
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    MaterialTheme {
        when (val screen = currentScreen) {
            is Screen.Home           -> {
                HomePage(
                    onWordItemClick = { word ->
                        currentScreen = Screen.Detail(word) // Navigate to Detail
                    },
                    onNavigate = { route ->
                        println("Bottom nav clicked: $route")
                        when (route) {
                            "home"     -> currentScreen = Screen.Home // Stay home or return home
                            "review" -> currentScreen =
                                Screen.Review(sampleWordItem) // Navigate to Review (using sample for now)
                            "settings" -> currentScreen = Screen.Settings // Navigate to Settings
                            "fetch"    -> currentScreen = Screen.WordFetch // Navigate to WordFetch via FAB
                        }
                    }
                )
            }

            is Screen.Detail         -> {
                WordDetailPage(
                    wordItem = screen.word,
                    onBack = {
                        currentScreen = Screen.Home // Navigate back to Home from Detail
                    }
                )
            }

            is Screen.Review         -> {
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

            is Screen.Settings       -> {
                SettingsPage(
                    onNavigateBack = { currentScreen = Screen.Home }, // Navigate back to Home
                    // Add other callbacks as needed, e.g.:
                    onLogout = {
                        println("Logout clicked!")
                        // Implement actual logout logic and navigate (e.g., to a login screen or back home)
                        currentScreen = Screen.Home // Example: Go back home after logout
                    },
                    onEditProfile = {
                        currentScreen = Screen.EditProfile
                    }, // Navigate to EditProfile
                    onChangeApiKey = {
                        currentScreen = Screen.ApiKeySettings
                    }, // Navigate to ApiKeySettings
                    onNotificationSettings = { println("Notification Settings clicked!") },
                    onIntroduction = { println("Introduction clicked!") },
                    onTermsOfService = { println("Terms of Service clicked!") }
                )
            }

            is Screen.EditProfile    -> {
                EditProfilePage(
                    onNavigateBack = { currentScreen = Screen.Settings }, // Go back to Settings
                    onSaveChanges = { name, city, state, bio ->
                        println("Saving Profile: Name=$name, City=$city, State=$state, Bio=$bio")
                        // Add actual save logic here
                        currentScreen = Screen.Settings // Navigate back to Settings after save
                    }
                )
            }

            is Screen.ApiKeySettings -> {
                ApiKeySettingPage(
                    onNavigateBack = { currentScreen = Screen.Settings }, // Go back to Settings
                    onSaveChanges = { name, city, state ->
                        println("Saving API Key Settings: Name=$name, City=$city, State=$state")
                        // Add actual save logic here (likely involves API key field not shown in design)
                        currentScreen = Screen.Settings // Navigate back to Settings after save
                    }
                )
            }
            is Screen.WordFetch -> {
                WordFetchPage(
                    onBack = { currentScreen = Screen.Home } // Navigate back to Home (or previous screen)
                    // Pass fetched data if available, handle search logic etc.
                )
            }
        }
    }
}
