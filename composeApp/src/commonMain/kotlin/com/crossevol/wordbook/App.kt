package com.crossevol.wordbook

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.crossevol.wordbook.data.ApiKeyConfigRepository // Import repository
import com.crossevol.wordbook.data.api.WordFetchApi // Import API client
import com.crossevol.wordbook.data.SettingsRepository // Import SettingsRepository
import com.crossevol.wordbook.data.WordRepository // Import WordRepository
import com.crossevol.wordbook.data.model.WordItemUI
import com.crossevol.wordbook.db.AppDatabase
import com.crossevol.wordbook.db.createDatabase
import com.crossevol.wordbook.db.initializeDatabase // Import initializer
import com.crossevol.wordbook.ui.screens.ApiKeyConfig
import com.crossevol.wordbook.ui.screens.ApiKeyEditingPage // Import the editing page
import com.crossevol.wordbook.ui.screens.ApiKeyListPage
import com.crossevol.wordbook.ui.screens.EditProfilePage
import com.crossevol.wordbook.ui.screens.HomePage
import com.crossevol.wordbook.ui.screens.SettingsPage
import com.crossevol.wordbook.ui.screens.WordDetailPage
import com.crossevol.wordbook.ui.screens.WordFetchPage
import com.crossevol.wordbook.ui.screens.WordDetailSummaryPage // Import the new summary page
import com.crossevol.wordbook.ui.screens.WordReviewPage
import com.crossevol.wordbook.ui.viewmodel.WordFetchViewModel // Import ViewModel
import com.crossevol.wordbook.ui.viewmodel.WordReviewViewModel
import com.crossevol.wordbook.ui.viewmodel.ApiKeyViewModel
import com.russhwolf.settings.Settings
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers // Import Dispatchers for background tasks
import kotlinx.coroutines.withContext // Import withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

// Add imports for Scaffold and Snackbar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration // Add this import
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult // Add this import
import androidx.compose.runtime.rememberCoroutineScope // Import rememberCoroutineScope
import kotlinx.coroutines.launch // Import launch


private val logger = KotlinLogging.logger {} // Add logger instance

// Define screen states for navigation
sealed class Screen {
    data object Home : Screen()
    data class Detail(val word: WordItemUI) : Screen()
    data object Settings : Screen()
    data object EditProfile : Screen()
    data object ApiKeyList : Screen()
    data class ApiKeyEdit(val config: ApiKeyConfig? = null) : Screen() // config is optional for adding
    data object WordFetch : Screen() // Add WordFetch screen state
    data object WordDetailSummary : Screen() // Add state for the new summary page
    data class WordReview(val words: List<WordItemUI>, val currentIndex: Int = 0) : Screen() // Add state for review process
}

@Composable
@Preview
fun App(
    // Dependencies passed from platform-specific main functions
    settings: Settings? = null, // Pass Settings instance
    driverFactory: com.crossevol.wordbook.db.DriverFactory? = null, // Make nullable for Preview
    wordFetchApi: WordFetchApi? = null // <-- Add WordFetchApi parameter here (nullable for Preview)
) {
    // Navigation state using the sealed class
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    // Coroutine scope for launching tasks from non-composable contexts like button clicks
    val scope = rememberCoroutineScope()

    // --- Database and Repository Setup ---
    // Use remember to create database and repository instances, tied to the composable lifecycle
    val database: AppDatabase? = remember(driverFactory) {
        driverFactory?.let { createDatabase(it) }
    }

    val apiKeyConfigRepository: ApiKeyConfigRepository? = remember(database) {
        database?.let { ApiKeyConfigRepository(it) }
    }

    // Create Word Repository instance
    val wordRepository: WordRepository? = remember(database) {
        database?.let { WordRepository(it) }
    }

    // Create Settings Repository instance
    val settingsRepository: SettingsRepository? = remember(settings) {
        settings?.let { SettingsRepository(it) }
    }

    // Initialize database with dummy data if empty, runs once
    LaunchedEffect(database, apiKeyConfigRepository, wordRepository) { // Add wordRepository to key
        if (database != null && apiKeyConfigRepository != null && wordRepository != null) { // Check wordRepository too
            initializeDatabase(database, apiKeyConfigRepository, wordRepository) // Pass wordRepository
        }
    }
    // --- End Database Setup ---


    // Removed: val wordFetchApi = remember { WordFetchApi() } // This is now passed as a parameter


    // Create ViewModel for API Key management
    // Use remember to create the ViewModel, handling the nullable repository
    val apiKeyViewModel = remember(apiKeyConfigRepository) {
        apiKeyConfigRepository?.let { ApiKeyViewModel(it) } // Return null if repository is null
    }

    // Create ViewModel for WordFetchPage, passing the API client and the *ApiKeyViewModel*
    val wordFetchViewModel = remember(wordFetchApi, apiKeyViewModel, wordRepository) { // Dependency on apiKeyViewModel
        // Ensure dependencies are not null before passing
        if (wordFetchApi != null && apiKeyViewModel != null) { // wordRepository can be null for previews
             WordFetchViewModel(api = wordFetchApi, apiKeyViewModel = apiKeyViewModel, wordRepository = wordRepository) // Pass apiKeyViewModel
        } else null // Return null if dependencies aren't ready
    }

    // Create ViewModel for WordReviewPage
    val wordReviewViewModel = remember(wordRepository) {
        // Ensure wordRepository is not null before creating ViewModel
        wordRepository?.let { WordReviewViewModel(it) } // Return null if repository is null
    }


    MaterialTheme {
        // Wrap the screen content with Scaffold to provide SnackbarHost
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomePage(
                        settingsRepository = settingsRepository, // Pass the settings repository
                        wordRepository = wordRepository, // Pass the word repository
                        snackbarHostState = snackbarHostState, // Pass SnackbarHostState
                        onWordItemClick = { word ->
                            logger.info { "Navigating to Detail for word: ${word.title}" } // Replaced println
                            currentScreen = Screen.Detail(word) // Navigate to Detail
                        },
                        onNavigate = { route ->
                            logger.info { "Bottom nav clicked: $route" } // Replaced println
                            when (route) {
                                "home" -> currentScreen = Screen.Home // Stay home or return home
                                "review" -> currentScreen =
                                    Screen.WordDetailSummary // Navigate to the new summary page
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

                is Screen.Settings -> {
                    SettingsPage(
                        onNavigateBack = {
                            logger.info { "Navigating back to Home from Settings." } // Replaced println
                            currentScreen = Screen.Home
                        }, // Navigate back to Home
                        // Implement the export functionality
                        onExport = { path, format -> // Removed @Composable
                            logger.info { "Exporting data in $format format to $path" }
                            // Use the scope captured from the App composable
                            scope.launch { // Launch coroutine for export logic
                                if (wordRepository != null) {
                                    // Export words using the repository (assuming exportWords is suspend or runs on background thread)
                                    val exportedFilePath = wordRepository.exportWords(path, format)

                                    if (exportedFilePath != null) {
                                        // Show success message with an action button
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Export successful: $exportedFilePath",
                                            actionLabel = "Open Folder", // Add the action button label
                                            duration = SnackbarDuration.Long // Keep snackbar visible longer
                                        )
                                        // If the action button was clicked, open the directory
                                        if (result == SnackbarResult.ActionPerformed) {
                                            openFileExplorer(path)
                                        }
                                    } else {
                                        // Show error message
                                        snackbarHostState.showSnackbar("Export failed")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("Error: Repository not initialized")
                                }
                            }
                        },
                       // Implement the import functionality
                       onImport = { path, format ->
                           logger.info { "Importing data from $path in $format format" }
                           scope.launch { // Launch coroutine for file reading and import
                               if (wordRepository != null) {
                                   try {
                                       // Read file content in the background
                                       val fileContent = withContext(Dispatchers.Default) {
                                           readFileContent(path)
                                       }

                                       if (fileContent != null) {
                                           // Import words using the repository in the background
                                           val importedCount = withContext(Dispatchers.Default) {
                                               wordRepository.importWords(fileContent, format)
                                           }

                                           if (importedCount != null) {
                                               snackbarHostState.showSnackbar("Successfully imported $importedCount words.")
                                               // Optionally refresh data or navigate
                                           } else {
                                               snackbarHostState.showSnackbar("Import failed: Unsupported format or error during processing.")
                                           }
                                       } else {
                                           snackbarHostState.showSnackbar("Import failed: Could not read file content.")
                                       }
                                   } catch (e: Exception) {
                                       logger.error(e) { "Error during import: ${e.message}" }
                                       snackbarHostState.showSnackbar("Import failed: An unexpected error occurred.")
                                   }
                               } else {
                                   snackbarHostState.showSnackbar("Error: Repository not initialized")
                               }
                           }
                       },
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

                is Screen.ApiKeyList -> { // New case for the API Key List page
                    // Pass the ViewModel to the list page, handling the nullable case
                    if (apiKeyViewModel != null) {
                        ApiKeyListPage(
                            viewModel = apiKeyViewModel, // Provide the ViewModel
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
                            } // Navigate to Edit page with config
                            // onDeleteApiKey is now handled inside ApiKeyListPage by calling the ViewModel
                        )
                    } else {
                         // Handle the case where the repository/ViewModel is not available (e.g., in preview)
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             Text("Error: API Key Repository not initialized.")
                         }
                    }
                }

                is Screen.WordFetch -> {
                    // Ensure ViewModels are not null before using
                    if (wordFetchViewModel != null && apiKeyViewModel != null) { // Check both ViewModels
                        WordFetchPage( // Use the ViewModel instance created above
                            viewModel = wordFetchViewModel, // Pass the WordFetchViewModel
                            apiKeyViewModel = apiKeyViewModel, // Pass the ApiKeyViewModel
                            snackbarHostState = snackbarHostState, // Pass SnackbarHostState
                            onBack = {
                                logger.info { "Navigating back to Home from WordFetch." } // Replaced println
                                currentScreen = Screen.Home
                            } // Navigate back to Home (or previous screen)
                        )
                    } else {
                        // Handle the case where the repository/ViewModel is not available (e.g., in preview)
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: Required ViewModels not initialized.")
                        }
                    }
                }

                is Screen.ApiKeyEdit -> { // Handle the ApiKeyEdit screen state
                    // Pass the ViewModel to the editing page, handling the nullable case
                    if (apiKeyViewModel != null) {
                        ApiKeyEditingPage(
                            viewModel = apiKeyViewModel, // Provide the ViewModel
                            config = screen.config, // Pass the config from the screen state
                            onNavigateBack = {
                                logger.info { "Navigating back to ApiKeyList from ApiKeyEdit." } // Replaced println
                                currentScreen = Screen.ApiKeyList
                            } // Go back to the list after saving (handled inside the page now)
                            // onSaveChanges is now handled inside ApiKeyEditingPage by calling the ViewModel
                        )
                    } else {
                        // Handle the case where the repository/ViewModel is not available (e.g., in preview)
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: API Key Repository not initialized.")
                        }
                    }
                }

                is Screen.WordDetailSummary -> { // Add case for the new summary page
                    // Pass the ViewModel to the summary page, handling the nullable case
                    if (wordReviewViewModel != null) {
                        WordDetailSummaryPage(
                            viewModel = wordReviewViewModel,
                            onStart = { wordList ->
                                logger.info { "Starting review with ${wordList.size} words" }
                                currentScreen = Screen.WordReview(wordList) // Navigate to review page with words
                            },
                            onBack = {
                                logger.info { "Navigating back to Home from WordDetailSummary." }
                                currentScreen = Screen.Home // Navigate back to Home
                            }
                        )
                    } else {
                         // Handle the case where the repository/ViewModel is not available (e.g., in preview)
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             Text("Error: Word Repository not initialized.")
                         }
                    }
                }

                is Screen.WordReview -> {
                    val reviewState = screen as Screen.WordReview
                    val words = reviewState.words
                    val currentIndex = reviewState.currentIndex

                    // Ensure ViewModel is not null before using
                    if (wordReviewViewModel != null) {
                        if (currentIndex < words.size) {
                            WordReviewPage(
                                wordItem = words[currentIndex],
                                remainingWordsCount = words.size - currentIndex, // Pass the count of remaining words
                                onRemember = {
                                    // Update word progress (remembered)
                                    wordReviewViewModel.updateWordReviewProgress(words[currentIndex].id, true)
                                    // Navigation logic is now in onNext
                                },
                                onForget = {
                                    // Update word progress (forgotten)
                                    wordReviewViewModel.updateWordReviewProgress(words[currentIndex].id, false)
                                    // Navigation logic is now in onNext
                                },
                                onSkip = {
                                    // Skip this word without updating progress
                                    wordReviewViewModel.skipWordReview(words[currentIndex].id)
                                    // Navigation logic is now in onNext
                                },
                                onBack = {
                                    // Cancel review and go back to summary
                                    currentScreen = Screen.WordDetailSummary
                                },
                                onNext = {
                                    // Navigation logic: move to next word or back to summary if done
                                    if (currentIndex + 1 < words.size) {
                                        currentScreen = Screen.WordReview(words, currentIndex + 1)
                                    } else {
                                        // All words reviewed, refresh data and go back to summary
                                        wordReviewViewModel.loadWordsForReview() // Refresh data
                                        currentScreen = Screen.WordDetailSummary
                                    }
                                },
                            )
                        } else {
                            // Safety check - if no more words, go back to summary
                            wordReviewViewModel.loadWordsForReview() // Refresh data
                            currentScreen = Screen.WordDetailSummary
                        }
                    } else {
                         // Handle the case where the repository/ViewModel is not available (e.g., in preview)
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             Text("Error: Word Repository not initialized.")
                         }
                    }
                }

                Screen.EditProfile   -> TODO()
            }
        }
    }
}
