package com.crossevol.wordbook

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import com.crossevol.wordbook.data.ApiKeyConfigRepository // Import repository
import com.crossevol.wordbook.data.api.WordFetchApi // Import API client
import com.crossevol.wordbook.data.model.WordItem
import com.crossevol.wordbook.db.AppDatabase
import com.crossevol.wordbook.db.createDatabase
import com.crossevol.wordbook.db.initializeDatabase // Import initializer
import com.crossevol.wordbook.ui.components.sampleWordItem
import com.crossevol.wordbook.ui.screens.ApiKeyConfig
import com.crossevol.wordbook.ui.screens.ApiKeyEditingPage // Import the editing page
import com.crossevol.wordbook.ui.screens.ApiKeyListPage
import com.crossevol.wordbook.ui.screens.EditProfilePage
import com.crossevol.wordbook.ui.screens.HomePage
import com.crossevol.wordbook.ui.screens.SettingsPage
import com.crossevol.wordbook.ui.screens.WordDetailPage
import com.crossevol.wordbook.ui.screens.WordFetchPage
import com.crossevol.wordbook.ui.screens.WordReviewPage
import com.crossevol.wordbook.ui.viewmodel.WordFetchViewModel // Import ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging // Import KotlinLogging
import org.jetbrains.compose.ui.tooling.preview.Preview

private val logger = KotlinLogging.logger {} // Add logger instance

// Define screen states for navigation
sealed class Screen {
    object Home : Screen()
    data class Detail(val word: WordItem) : Screen()
    data class Review(val word: WordItem) : Screen()
    object Settings : Screen()
    object EditProfile : Screen()
    object ApiKeyList : Screen()
    data class ApiKeyEdit(val config: ApiKeyConfig? = null) : Screen() // config is optional for adding
    object WordFetch : Screen() // Add WordFetch screen state
}

@Composable
@Preview
fun App(
    // Pass DriverFactory from platform-specific main functions
    driverFactory: com.crossevol.wordbook.db.DriverFactory? = null // Make nullable for Preview
) {
    // Navigation state using the sealed class
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // --- Database and Repository Setup ---
    // Use remember to create database and repository instances, tied to the composable lifecycle
    val database: AppDatabase? = remember(driverFactory) {
        driverFactory?.let { createDatabase(it) }
    }

    val apiKeyConfigRepository: ApiKeyConfigRepository? = remember(database) {
        database?.let { ApiKeyConfigRepository(it) }
    }

    // Track when to refresh API keys list
    var apiKeyListRefreshTrigger by remember { mutableStateOf(0) }

    // Initialize database with dummy data if empty, runs once
    LaunchedEffect(database, apiKeyConfigRepository) {
        if (database != null && apiKeyConfigRepository != null) {
            initializeDatabase(database, apiKeyConfigRepository)
        }
    }
    // --- End Database Setup ---


    // TODO: Securely obtain API Key here or pass it from platform-specific code
    // This is a placeholder. Do NOT commit your actual API key.
    // You need to implement platform-specific logic to read this securely (e.g., from .env, secrets manager, etc.)
    // For now, we'll use a placeholder or fetch the *active* key from the DB later.
    val apiKey = "YOUR_API_KEY_HERE" // <<< REPLACE WITH SECURELY OBTAINED KEY

    // Create API client instance (can be singleton or managed by DI)
    // TODO: Update this to use the *active* API key from the database once implemented
    val wordFetchApi = remember { WordFetchApi(apiKey) }


    MaterialTheme {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomePage(
                    onWordItemClick = { word ->
                        logger.info { "Navigating to Detail for word: ${word.title}" } // Replaced println
                        currentScreen = Screen.Detail(word) // Navigate to Detail
                    },
                    onNavigate = { route ->
                        logger.info { "Bottom nav clicked: $route" } // Replaced println
                        when (route) {
                            "home" -> currentScreen = Screen.Home // Stay home or return home
                            "review" -> currentScreen =
                                Screen.Review(sampleWordItem) // Navigate to Review (using sample for now)
                            "settings" -> currentScreen = Screen.Settings // Navigate to Settings
                            "fetch" -> currentScreen =
                                Screen.WordFetch // Navigate to WordFetch via FAB
                        }
                    }
                )
            }

            is Screen.Detail -> {
                WordDetailPage(
                    wordItem = screen.word,
                    onBack = {
                        logger.info { "Navigating back to Home from Detail." } // Replaced println
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
                        logger.info { "Review action taken, returning home." } // Replaced println
                    },
                    onBack = {
                        // Explicit back navigation from Review screen, go back home
                        currentScreen = Screen.Home
                        logger.info { "Review back pressed, returning home." } // Replaced println
                    }
                )
            }

            is Screen.Settings -> {
                SettingsPage(
                    onNavigateBack = {
                        logger.info { "Navigating back to Home from Settings." } // Replaced println
                        currentScreen = Screen.Home
                    }, // Navigate back to Home
                    // Add other callbacks as needed, e.g.:
                    onLogout = {
                        logger.info { "Logout clicked!" } // Replaced println
                        // Implement actual logout logic and navigate (e.g., to a login screen or back home)
                        currentScreen = Screen.Home // Example: Go back home after logout
                    },
                    onEditProfile = {
                        logger.info { "Navigating to EditProfile." } // Replaced println
                        currentScreen = Screen.EditProfile
                    }, // Navigate to EditProfile
                    onChangeApiKey = {
                        logger.info { "Navigating to ApiKeyList." } // Replaced println
                        currentScreen = Screen.ApiKeyList // Navigate to the new ApiKeyListPage
                    },
                    onNotificationSettings = { logger.info { "Notification Settings clicked!" } }, // Replaced println
                    onIntroduction = { logger.info { "Introduction clicked!" } }, // Replaced println
                    onTermsOfService = { logger.info { "Terms of Service clicked!" } } // Replaced println
                )
            }

            is Screen.EditProfile -> {
                EditProfilePage(
                    onNavigateBack = {
                        logger.info { "Navigating back to Settings from EditProfile." } // Replaced println
                        currentScreen = Screen.Settings
                    }, // Go back to Settings
                    onSaveChanges = { name, city, state, bio ->
                        logger.info { "Saving Profile: Name=$name, City=$city, State=$state, Bio=$bio" } // Replaced println
                        // Add actual save logic here
                        currentScreen = Screen.Settings // Navigate back to Settings after save
                        logger.info { "Navigating back to Settings after saving profile." } // Replaced println
                    }
                )
            }

            is Screen.ApiKeyList -> { // New case for the API Key List page
                // Fetch the list of API keys from the repository whenever we navigate to this screen
                // or when the refresh trigger changes
                val apiKeyConfigs = remember(apiKeyConfigRepository, apiKeyListRefreshTrigger) {
                    apiKeyConfigRepository?.getAllApiKeyConfigs() ?: emptyList<ApiKeyConfig>().also {
                        logger.warn { "ApiKeyConfigRepository is null or returned empty list." } // Replaced println
                    }
                }

                ApiKeyListPage(
                    apiKeyConfigs = apiKeyConfigs, // Provide actual list from DB
                    onNavigateBack = {
                        logger.info { "Navigating back to Settings from ApiKeyList." } // Replaced println
                        currentScreen = Screen.Settings
                    }, // Go back to Settings
                    onAddApiKey = {
                        logger.info { "Navigating to ApiKeyEdit (Add mode)." } // Replaced println
                        currentScreen = Screen.ApiKeyEdit(null)
                    }, // Navigate to Edit page for adding (config is null)
                    onEditApiKey = { config ->
                        logger.info { "Navigating to ApiKeyEdit (Edit mode) for ID: ${config.id}" } // Replaced println
                        currentScreen = Screen.ApiKeyEdit(config)
                    }, // Navigate to Edit page with config
                    onDeleteApiKey = { config ->
                        // Handle delete action using the repository
                        apiKeyConfigRepository?.deleteApiKeyConfigById(config.id)
                        logger.info { "Deleted API Key: ${config.alias} (ID: ${config.id})" } // Replaced println
                        // After deleting, increment the refresh trigger to force refresh
                        apiKeyListRefreshTrigger++
                    }
                )
            }

            is Screen.WordFetch -> {
                // Create ViewModel using the proper androidx.lifecycle.viewmodel.compose.viewModel function
                val wordFetchViewModel = WordFetchViewModel(api = wordFetchApi)
                WordFetchPage(
                    viewModel = wordFetchViewModel, // Pass the ViewModel
                    onBack = {
                        logger.info { "Navigating back to Home from WordFetch." } // Replaced println
                        currentScreen = Screen.Home
                    } // Navigate back to Home (or previous screen)
                )
            }

            is Screen.ApiKeyEdit -> { // Handle the ApiKeyEdit screen state
                ApiKeyEditingPage(
                    config = screen.config, // Pass the config from the screen state
                    onNavigateBack = {
                        logger.info { "Navigating back to ApiKeyList from ApiKeyEdit." } // Replaced println
                        currentScreen = Screen.ApiKeyList
                    }, // Go back to the list
                    onSaveChanges = { alias, key, provider, model ->
                        // Implement actual saving logic here (e.g., to database/preferences)
                        if (apiKeyConfigRepository != null) {
                            val configToSave = ApiKeyConfig(
                                id = screen.config?.id ?: 0L, // Use existing ID if editing, 0L for new
                                alias = alias,
                                apiKey = key, // Use the entered key
                                provider = provider,
                                model = model
                            )
                            if (configToSave.id == 0L) {
                                // Insert new config
                                apiKeyConfigRepository.insertApiKeyConfig(configToSave)
                                logger.info { "Inserted new API Key: $alias" } // Replaced println
                            } else {
                                // Update existing config
                                apiKeyConfigRepository.updateApiKeyConfig(configToSave)
                                logger.info { "Updated API Key: $alias (ID: ${configToSave.id})" } // Replaced println
                            }
                            // Increment the refresh trigger to force refresh when returning to list
                            apiKeyListRefreshTrigger++
                        } else {
                            logger.error { "ApiKeyConfigRepository is not initialized. Cannot save API key." } // Replaced println
                        }
                        currentScreen = Screen.ApiKeyList // Go back to the list after saving
                        logger.info { "Navigating back to ApiKeyList after saving API key." } // Replaced println
                    }
                )
            }
        }
    }
}
