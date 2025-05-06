package com.crossevol.wordbook

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding // Import padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import com.crossevol.wordbook.data.model.WordItemUI // Keep this
import com.crossevol.wordbook.db.createDatabase // Keep this
import com.crossevol.wordbook.db.initializeDatabase // Keep this
import com.crossevol.wordbook.ui.screens.ApiKeyConfig // Keep this
import com.crossevol.wordbook.ui.screens.ApiKeyEditingPage // Keep this
import com.crossevol.wordbook.ui.screens.ApiKeyListPage
import com.crossevol.wordbook.ui.screens.HomePage
import com.crossevol.wordbook.ui.screens.SettingsPage
import com.crossevol.wordbook.ui.screens.WordDetailPage
import com.crossevol.wordbook.ui.screens.WordFetchPage
import com.crossevol.wordbook.ui.screens.WordDetailSummaryPage // Import the new summary page
import com.crossevol.wordbook.ui.screens.WordReviewPage
import com.crossevol.wordbook.ui.viewmodel.WordFetchViewModel // Keep this
import com.crossevol.wordbook.ui.viewmodel.WordReviewViewModel // Keep this
import com.crossevol.wordbook.ui.viewmodel.ApiKeyViewModel // Keep this
import com.russhwolf.settings.Settings // Keep this
import io.github.oshai.kotlinlogging.KotlinLogging // Keep this
import kotlinx.coroutines.Dispatchers // Keep this
import kotlinx.coroutines.withContext // Keep this
import org.jetbrains.compose.ui.tooling.preview.Preview

// Add imports for Scaffold and Snackbar
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration // Add this import
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState // Keep this
import androidx.compose.material.SnackbarResult // Keep this
import androidx.compose.runtime.rememberCoroutineScope // Keep this
import com.crossevol.wordbook.db.DriverFactory // Keep this
import androidx.compose.runtime.State // Import State for trigger
import kotlinx.coroutines.launch // Keep this
import com.crossevol.wordbook.ui.screens.EditProfilePage // Import EditProfilePage

private val logger = KotlinLogging.logger {} // Add logger instance

// Define screen states for navigation
sealed class Screen {
    data object Home : Screen()
    data class Detail(val word: WordItemUI) : Screen()
    data object Settings : Screen()
    data object EditProfile : Screen()
    data object ApiKeyList : Screen()
    data class ApiKeyEdit(val config: ApiKeyConfig? = null) :
        Screen() // config is optional for adding

    data object WordFetch : Screen() // Add WordFetch screen state
    data object WordDetailSummary : Screen() // Add state for the new summary page
    data class WordReview(
        val words: List<WordItemUI>,
        val currentIndex: Int = 0
    ) : Screen() // Add state for review process
}

@Composable
// @Preview // Preview might be harder to set up now with internal state management
fun App(
    // Core dependencies provided by each platform
    driverFactory: DriverFactory,
    settings: Settings,
    wordFetchApi: WordFetchApi, // API client needs platform-specific engine config

    // Optional navigation triggers
    initialScreenRoute: String? = null, // For Android intent navigation
    desktopReviewTrigger: State<Boolean>? = null, // For Desktop notification click
    onDesktopReviewTriggerHandled: (() -> Unit)? = null // Callback for Desktop trigger
) {
    // --- Internal State Management ---

    // Create database and repositories internally, remembered across recompositions
    val database = remember(driverFactory) { createDatabase(driverFactory) }
    val wordRepository = remember(database) { WordRepository(database) }
    val apiKeyConfigRepository = remember(database) { ApiKeyConfigRepository(database) }
    val settingsRepository = remember(settings) { SettingsRepository(settings) }

    // Create ViewModels internally
    val apiKeyViewModel =
        remember(apiKeyConfigRepository) { ApiKeyViewModel(apiKeyConfigRepository) }
    // Ensure WordFetchViewModel receives non-null WordRepository
    val wordFetchViewModel = remember(
        wordFetchApi,
        apiKeyViewModel,
        wordRepository
    ) {
        WordFetchViewModel(
            wordFetchApi,
            apiKeyViewModel,
            wordRepository
        )
    }
    val wordReviewViewModel = remember(wordRepository) { WordReviewViewModel(wordRepository) }

    // Navigation state managed internally
    var currentScreenState by remember { mutableStateOf<Screen>(Screen.Home) }
    val onScreenChange: (Screen) -> Unit = { newScreen ->
        logger.debug { "Changing screen to: $newScreen" }
        currentScreenState = newScreen
    }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    // Coroutine scope for launching tasks like export/import
    val scope = rememberCoroutineScope()

    // Initialize database with dummy data if empty, runs once
    LaunchedEffect(
        database,
        apiKeyConfigRepository,
        wordRepository
    ) {
        // Pass the internally created instances
        initializeDatabase(
            database,
            apiKeyConfigRepository,
            wordRepository
        )
    }

    // Handle navigation trigger from Desktop notification
    LaunchedEffect(desktopReviewTrigger?.value) {
        if (desktopReviewTrigger?.value == true) {
            logger.info { "Desktop review trigger activated. Navigating to WordDetailSummary." }
            // Use the internal state modifier
            onScreenChange(Screen.WordDetailSummary)
            onDesktopReviewTriggerHandled?.invoke() // Signal that the trigger has been handled
        }
    }

    // --- UI ---
    // No explicit isLoading check needed here anymore, dependencies are created synchronously with remember
    MaterialTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues -> // Consume padding values
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) { // Apply padding
                // Use the internal currentScreenState
                when (val screen = currentScreenState) {
                    is Screen.Home              -> {
                        HomePage(
                            // Pass the internally created repositories/VMs
                            settingsRepository = settingsRepository,
                            wordRepository = wordRepository,
                            snackbarHostState = snackbarHostState,
                            onWordItemClick = { word ->
                                logger.info { "Navigating to Detail for word: ${word.title}" }
                                onScreenChange(Screen.Detail(word)) // Use internal callback
                            },
                            onNavigate = { route ->
                                logger.info { "Bottom nav clicked: $route" }
                                when (route) {
                                    "home"     -> onScreenChange(Screen.Home) // Use internal callback
                                    "review"   -> onScreenChange(Screen.WordDetailSummary) // Use internal callback
                                    "settings" -> onScreenChange(Screen.Settings) // Use internal callback
                                    "fetch"    -> onScreenChange(Screen.WordFetch) // Use internal callback
                                }
                            }
                        )
                    }

                    is Screen.Detail            -> {
                        WordDetailPage(
                            wordItem = screen.word,
                            onBack = {
                                logger.info { "Navigating back to Home from Detail." }
                                onScreenChange(Screen.Home) // Use internal callback
                            }
                        )
                    }

                    is Screen.Settings          -> {
                        SettingsPage(
                            // Pass the internally created repository
                            settingsRepository = settingsRepository,
                            onNavigateBack = {
                                logger.info { "Navigating back to Home from Settings." }
                                onScreenChange(Screen.Home) // Use internal callback
                            },
                            onExport = { path, format ->
                                logger.info { "Exporting data in $format format to $path" }
                                scope.launch {
                                    // Use the internally created repository
                                    val exportedFilePath = wordRepository.exportWords(
                                        path,
                                        format
                                    )

                                    if (exportedFilePath != null) {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Export successful: $exportedFilePath",
                                            actionLabel = "Open Folder",
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            openFileExplorer(path)
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar("Export failed")
                                    }
                                }
                            },
                            onImport = { path, format ->
                                logger.info { "Importing data from $path in $format format" }
                                scope.launch {
                                    // Use the internally created repository
                                    try {
                                        val fileContent = withContext(Dispatchers.Default) {
                                            readFileContent(path)
                                        }

                                        if (fileContent != null) {
                                            val importedCount = withContext(Dispatchers.Default) {
                                                wordRepository.importWords(
                                                    fileContent,
                                                    format
                                                )
                                            }

                                            if (importedCount != null) {
                                                snackbarHostState.showSnackbar("Successfully imported $importedCount words.")
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
                                    // Removed the else block checking for null repository
                                }
                            },
                            onLogout = {
                                logger.info { "Logout clicked!" }
                                onScreenChange(Screen.Home) // Use internal callback
                            },
                            onEditProfile = {
                                logger.info { "Navigating to EditProfile." }
                                onScreenChange(Screen.EditProfile) // Use internal callback
                            },
                            onChangeApiKey = {
                                logger.info { "Navigating to ApiKeyList." }
                                onScreenChange(Screen.ApiKeyList) // Use internal callback
                            },
                            onIntroduction = { logger.info { "Introduction clicked!" } },
                            onTermsOfService = { logger.info { "Terms of Service clicked!" } }
                        )
                    }

                    is Screen.ApiKeyList        -> {
                        // Use the internally created ViewModel
                        ApiKeyListPage(
                            viewModel = apiKeyViewModel,
                            onNavigateBack = {
                                logger.info { "Navigating back to Settings from ApiKeyList." }
                                onScreenChange(Screen.Settings) // Use internal callback
                            },
                            onAddApiKey = {
                                logger.info { "Navigating to ApiKeyEdit (Add mode)." }
                                onScreenChange(Screen.ApiKeyEdit(null)) // Use internal callback
                            },
                            onEditApiKey = { config ->
                                logger.info { "Navigating to ApiKeyEdit (Edit mode) for ID: ${config.id}" }
                                onScreenChange(Screen.ApiKeyEdit(config)) // Use internal callback
                            }
                        )
                        // Removed null check and ErrorScreen
                    }

                    is Screen.WordFetch         -> {
                        // Use the internally created ViewModels
                        WordFetchPage(
                            viewModel = wordFetchViewModel,
                            apiKeyViewModel = apiKeyViewModel,
                            snackbarHostState = snackbarHostState,
                            onBack = {
                                logger.info { "Navigating back to Home from WordFetch." }
                                onScreenChange(Screen.Home) // Use internal callback
                            }
                        )
                        // Removed null check and ErrorScreen
                    }

                    is Screen.ApiKeyEdit        -> {
                        // Use the internally created ViewModel
                        ApiKeyEditingPage(
                            viewModel = apiKeyViewModel,
                            config = screen.config,
                            onNavigateBack = {
                                logger.info { "Navigating back to ApiKeyList from ApiKeyEdit." }
                                onScreenChange(Screen.ApiKeyList) // Use internal callback
                            }
                        )
                        // Removed null check and ErrorScreen
                    }

                    is Screen.WordDetailSummary -> {
                        // Use the internally created ViewModel
                        WordDetailSummaryPage(
                            viewModel = wordReviewViewModel,
                            onStart = { wordList ->
                                logger.info { "Starting review with ${wordList.size} words" }
                                onScreenChange(Screen.WordReview(wordList)) // Use internal callback
                            },
                            onBack = {
                                logger.info { "Navigating back to Home from WordDetailSummary." }
                                onScreenChange(Screen.Home) // Use internal callback
                            }
                        )
                        // Removed null check and ErrorScreen
                    }

                    is Screen.WordReview        -> {
                        val reviewState = screen // Already cast
                        val words = reviewState.words
                        val currentIndex = reviewState.currentIndex

                        // Use the internally created ViewModel
                        if (currentIndex < words.size) {
                            WordReviewPage(
                                wordItem = words[currentIndex],
                                remainingWordsCount = words.size - currentIndex,
                                onRemember = {
                                    wordReviewViewModel.updateWordReviewProgress(
                                        words[currentIndex].id,
                                        true
                                    )
                                    // Navigation handled by onNext callback
                                },
                                onForget = {
                                    wordReviewViewModel.updateWordReviewProgress(
                                        words[currentIndex].id,
                                        false
                                    )
                                    // Navigation handled by onNext callback
                                },
                                onSkip = {
                                    wordReviewViewModel.skipWordReview(words[currentIndex].id)
                                    // Navigation handled by onNext callback
                                },
                                onBack = {
                                    // Cancel review and go back to summary
                                    onScreenChange(Screen.WordDetailSummary) // Use internal callback
                                },
                                onNext = {
                                    // Navigation logic: move to next word or back to summary if done
                                    if (currentIndex + 1 < words.size) {
                                        onScreenChange(
                                            Screen.WordReview(
                                                words,
                                                currentIndex + 1
                                            )
                                        ) // Use internal callback
                                    } else {
                                        // All words reviewed, refresh data and go back to summary
                                        wordReviewViewModel.loadWordsForReview() // Refresh data
                                        onScreenChange(Screen.WordDetailSummary) // Use internal callback
                                    }
                                },
                            )
                        } else {
                            // Safety check - if no more words, go back to summary
                            wordReviewViewModel.loadWordsForReview() // Refresh data
                            onScreenChange(Screen.WordDetailSummary) // Use internal callback
                        }
                        // Removed null check and ErrorScreen
                    }

                    Screen.EditProfile          -> {
                        // Render the actual EditProfilePage
                        EditProfilePage(
                            onNavigateBack = {
                                logger.info { "Navigating back to Settings from EditProfile." }
                                onScreenChange(Screen.Settings) // Navigate back to Settings
                            },
                            onSaveChanges = { name, city, state, bio ->
                                logger.info { "Saving profile changes: Name=$name, City=$city, State=$state, Bio=$bio" }
                                // TODO: Implement actual saving logic here
                                // After saving, navigate back to Settings or Home
                                onScreenChange(Screen.Settings) // Navigate back to Settings after saving
                            }
                        )
                    }
                }
                // Removed the closing brace for the 'else' block of isLoading check
            }
        }
    }
}

// Simple error screen composable (Keep this)
@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Error: $message")
    }
}
