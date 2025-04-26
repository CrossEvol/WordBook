package com.crossevol.wordbook.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossevol.wordbook.data.ApiKeyConfigRepository // Import repository
import com.crossevol.wordbook.data.api.WordFetchApi // Dependency on the API client
import com.crossevol.wordbook.data.api.WordFetchResultJson
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {} // Add logger instance

class WordFetchViewModel(
    private val api: WordFetchApi, // Dependency on the API client
    private val apiKeyConfigRepository: ApiKeyConfigRepository // Add repository dependency
) : ViewModel() {

    var searchQuery by mutableStateOf("")
    var modelOptions by mutableStateOf<List<String>>(emptyList()) // State for available models
    var selectedModel by mutableStateOf("") // State for selected model

    var selectedLanguageTabIndex by mutableStateOf(0)
    val languageTabs = listOf("EN", "JA", "ZH") // Language tabs

    var isLoading by mutableStateOf(false)
    var fetchedResult by mutableStateOf<WordFetchResultJson?>(null) // Use specific type
    var errorMessage by mutableStateOf<String?>(null)

    init {
        // Load available models from the repository when the ViewModel is created
        loadModelOptions()
    }

    private fun loadModelOptions() {
        val configs = apiKeyConfigRepository.getAllApiKeyConfigs()
        modelOptions = configs.map { it.model }.distinct() // Get unique models
        selectedModel = modelOptions.firstOrNull() ?: "" // Select the first available model, or empty
        logger.debug { "Loaded model options: $modelOptions, selected: $selectedModel" }
    }

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

        // Find the API key for the selected model
        val apiKeyConfig = apiKeyConfigRepository.getAllApiKeyConfigs().find { it.model == selectedModel }

        if (apiKeyConfig == null) {
            errorMessage = "No API key configured for the selected model: $selectedModel"
            logger.warn { "Fetch attempt failed: No API key for model '$selectedModel'." }
            return
        }

        val apiKey = apiKeyConfig.apiKey

        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
             errorMessage = "The API key for model '$selectedModel' is not configured."
             logger.warn { "Fetch attempt failed: API key for model '$selectedModel' is blank or placeholder." }
             return
        }

        logger.info { "Fetching word details for query: '$searchQuery' using model '$selectedModel'." }
        isLoading = true
        errorMessage = null // Clear previous errors
        fetchedResult = null // Clear previous results

        viewModelScope.launch {
            try {
                // Call the API using the current search query and the fetched API key
                val result = api.fetchWordDetails(searchQuery, apiKey) // Pass query and dynamic apiKey
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
        selectedModel = modelOptions.firstOrNull() ?: "" // Reset to first available model or empty
        selectedLanguageTabIndex = 0
        isLoading = false
        fetchedResult = null
        errorMessage = null
        logger.info { "Word fetch page reset." } // Replaced println
    }

    fun dismissErrorDialog() {
        logger.debug { "Dismissing error dialog." } // Replaced println
        errorMessage = null
    }
}
