package com.crossevol.wordbook.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossevol.wordbook.data.ApiKeyConfigRepository // Import repository - Keep for Mock
import com.crossevol.wordbook.data.api.WordFetchApi // Dependency on the API client
import com.crossevol.wordbook.data.api.WordFetchResultJson
import com.crossevol.wordbook.data.WordRepository // Import WordRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull // Import firstOrNull for StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers // Import Dispatchers

private val logger = KotlinLogging.logger {} // Add logger instance


class WordFetchViewModel(
    private val api: WordFetchApi, // Dependency on the API client
    // Removed ApiKeyConfigRepository dependency
    private val apiKeyViewModel: ApiKeyViewModel, // Add dependency on ApiKeyViewModel
    private val wordRepository: WordRepository? // Add WordRepository dependency (nullable for previews)
) : ViewModel() {

    // --- State Properties ---

    // UI State for the search query input
    var searchQuery by mutableStateOf("")

    // State for model selection dropdown options
    // modelOptions is now sourced from ApiKeyViewModel, so it's removed here.
    // var modelOptions by mutableStateOf<List<String>>(emptyList()) // State for model options

    // State for selected model
    var selectedModel by mutableStateOf("")

    var selectedLanguageTabIndex by mutableStateOf(0)
    val languageTabs = listOf(
        "EN",
        "JA",
        "ZH"
    ) // Language tabs

    var isLoading by mutableStateOf(false)
    var fetchedResult by mutableStateOf<WordFetchResultJson?>(null) // Use specific type
    var errorMessage by mutableStateOf<String?>(null)

    // State to track if the current fetched result has been saved
    var isResultSaved by mutableStateOf(false)
        private set // Only ViewModel can set this internally

    // State to track if a save operation is in progress
    var isSaving by mutableStateOf(false)
        private set // Only ViewModel can set this internally

    // Channel for sending one-time events to the UI (like Snackbar messages)
    private val _snackbarEvent = Channel<String>(Channel.BUFFERED)
    val snackbarEvent = _snackbarEvent.receiveAsFlow()


    init {
        // Initialize selectedModel based on the current state of ApiKeyViewModel's configs
        // Note: This only sets the initial value. The UI will observe ApiKeyViewModel's
        // apiKeyConfigs flow for updates to the list of available models.
        selectedModel = apiKeyViewModel.apiKeyConfigs.value.firstOrNull()?.model ?: ""
        logger.debug { "WordFetchViewModel initialized. Initial selected model: $selectedModel" }
    }

    // Removed loadModelOptions function

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun onModelSelect(model: String) {
        selectedModel = model
        // TODO: If the API URL changes based on model, update the API client or logic here
        logger.debug { "Model selected: $model" } // Replaced println
    }

    fun onLanguageTabSelect(index: Int) {
        selectedLanguageTabIndex = index
        logger.debug { "Language tab selected: ${languageTabs[index]} (index $index)" } // Replaced println
    }

    fun fetchWord() {
        if (searchQuery.isBlank()) {
            errorMessage = "Please enter a word or phrase to translate."
            logger.warn { "Fetch attempt with blank query." } // Replaced println
            return
        }
        if (isLoading) {
            logger.debug { "Fetch already in progress, ignoring new request." } // Replaced println
            return // Prevent multiple requests
        }

        logger.info { "Fetching word details for query: '$searchQuery' using model '$selectedModel'." }
        isLoading = true
        errorMessage = null // Clear previous errors
        fetchedResult = null // Clear previous result
        isResultSaved = false // Reset saved state for new fetch
        isSaving = false // Reset saving state


        // Find the API key for the selected model from ApiKeyViewModel's current state
        val apiKeyConfig =
            apiKeyViewModel.apiKeyConfigs.value.find { it.model == selectedModel }

        if (apiKeyConfig == null) {
            errorMessage = "No API key configured for the selected model: $selectedModel"
            logger.warn { "Fetch attempt failed: No API key for model '$selectedModel'." }
            isLoading = false // Stop loading indicator
            return
        }

        val apiKey = apiKeyConfig.apiKey

        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            errorMessage = "The API key for model '$selectedModel' is not configured."
            logger.warn { "Fetch attempt failed: API key for model '$selectedModel' is blank or placeholder." }
            isLoading = false // Stop loading indicator
            return
        }


        viewModelScope.launch {
            try {
                // Call the API using the current search query and the fetched API key
                val result = api.fetchWordDetails(
                    searchQuery,
                    apiKey
                ) // Pass query and dynamic apiKey
                fetchedResult = result
                logger.info { "Successfully fetched word details for '$searchQuery'." } // Replaced println
                // Automatically switch to the language tab that matches the input language?
                // Or just stay on the current tab. Let's stay on the current tab for now.

            } catch (e: Exception) {
                logger.error(e) { "Fetch Error in ViewModel: ${e.message}" } // Replaced println, added exception
                errorMessage = "Error fetching data: ${e.message}"
            } finally {
                isLoading = false
                logger.debug { "Fetch process finished." } // Replaced println
            }
        }
    }

    fun resetPage() {
        searchQuery = ""
        // Reset selectedModel based on the current state of ApiKeyViewModel's configs
        selectedModel = apiKeyViewModel.apiKeyConfigs.value.firstOrNull()?.model ?: ""
        selectedLanguageTabIndex = 0
        isLoading = false
        fetchedResult = null
        errorMessage = null
        isResultSaved = false // Reset saved state
        isSaving = false // Reset saving state
        logger.info { "Word fetch page reset." } // Replaced println
    }

    fun dismissErrorDialog() {
        logger.debug { "Dismissing error dialog." } // Replaced println
        errorMessage = null
    }

    /**
     * Saves the currently fetched word details to the database.
     */
    fun saveFetchedWord() {
        val result = fetchedResult ?: return // Only save if there's a result
        if (isSaving || isResultSaved) return // Don't save if already saving or saved
        if (wordRepository == null) {
            logger.error { "WordRepository is null. Cannot save word." }
            errorMessage = "Database not available. Cannot save." // Inform user
            viewModelScope.launch { _snackbarEvent.send("Error: Database not available.") } // Send snackbar event
            return
        }

        isSaving = true // Indicate saving process started
        logger.info { "Attempting to save word: ${result.text}" }

        viewModelScope.launch {
            try {
                // Save English details
                wordRepository.saveWordDetails(
                    title = result.text,
                    languageCode = "en",
                    explanation = result.enExplanation,
                    sentences = result.getEnSentencesList(),
                    pronunciation = result.enPronunciation,
                    relatedWords = result.getEnRelatedWordsList(),
                    rating = 0L // Default rating for new words
                )
                // Save Japanese details
                wordRepository.saveWordDetails(
                    title = result.text,
                    languageCode = "ja",
                    explanation = result.jaExplanation,
                    sentences = result.getJaSentencesList(),
                    pronunciation = result.jaPronunciation,
                    relatedWords = result.getJaRelatedWordsList(),
                    rating = 0L
                )
                // Save Chinese details
                wordRepository.saveWordDetails(
                    title = result.text,
                    languageCode = "zh",
                    explanation = result.zhExplanation,
                    sentences = result.getZhSentencesList(),
                    pronunciation = result.zhPronunciation,
                    relatedWords = result.getZhRelatedWordsList(),
                    rating = 0L
                )
                isResultSaved = true // Mark as saved successfully
                logger.info { "Successfully saved word: ${result.text}" }
                _snackbarEvent.send("Word '${result.text}' saved successfully!") // Send success message

            } catch (e: Exception) {
                logger.error(e) { "Failed to save word: ${result.text}" }
                errorMessage = "Failed to save word. Please try again." // Inform user
                _snackbarEvent.send("Failed to save word.") // Send error message
            } finally {
                isSaving = false // Indicate saving process finished (success or fail)
            }
        }
    }
}
