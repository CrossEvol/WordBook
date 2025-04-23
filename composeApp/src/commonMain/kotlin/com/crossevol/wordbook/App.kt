package com.crossevol.wordbook

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import com.crossevol.wordbook.data.model.WordItem
import com.crossevol.wordbook.ui.components.sampleWordItem
import com.crossevol.wordbook.ui.screens.ApiKeyConfig // Import ApiKeyConfig
import com.crossevol.wordbook.ui.screens.ApiKeyEditingPage // Import ApiKeyEditingPage (renamed)
import com.crossevol.wordbook.ui.screens.ApiKeyListPage // Import ApiKeyListPage (new)
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
    object ApiKeyList : Screen() // Renamed from ApiKeySettings to ApiKeyList
    data class ApiKeyEdit(val config: ApiKeyConfig? = null) : Screen() // New state for Add/Edit API Key
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
                        currentScreen = Screen.ApiKeyList // Navigate to the new ApiKeyListPage
                    },
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

            is Screen.ApiKeyList -> { // New case for the API Key List page
                ApiKeyListPage(
                    // apiKeyConfigs = ... // Provide actual list of saved keys here
                    onNavigateBack = { currentScreen = Screen.Settings }, // Go back to Settings
                    onAddApiKey = { currentScreen = Screen.ApiKeyEdit(null) }, // Navigate to Edit page for adding
                    onEditApiKey = { config -> currentScreen = Screen.ApiKeyEdit(config) }, // Navigate to Edit page with config
                    onDeleteApiKey = { config -> println("Delete API Key: ${config.alias}") } // Handle delete action
                )
            }

            is Screen.ApiKeyEdit -> { // Updated case for the API Key Editing page
                ApiKeyEditingPage(
                    config = screen.config, // Pass the config (null for add, object for edit)
                    onNavigateBack = { currentScreen = Screen.ApiKeyList }, // Go back to the List page
                    onSaveChanges = { alias, apiKey, provider, model ->
                        println("Saving API Key Settings: Alias=$alias, Provider=$provider, Model=$model, ApiKey=$apiKey")
                        // Add actual save logic here (distinguish add vs edit using screen.config?.id)
                        currentScreen = Screen.ApiKeyList // Navigate back to the List page after save
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
