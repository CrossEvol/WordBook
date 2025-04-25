package com.crossevol.wordbook.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossevol.wordbook.data.api.WordFetchApi
import com.crossevol.wordbook.data.api.WordFetchResultJson
import io.github.oshai.kotlinlogging.KotlinLogging // Import KotlinLogging
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {} // Add logger instance

class WordFetchViewModel(
    private val api: WordFetchApi // Dependency on the API client
) : ViewModel() {

    // State for the input field
    var searchQuery by mutableStateOf("")
        private set // Only ViewModel can change this directly

    // State for the dropdown selection
    val modelOptions = listOf(
        "gemini-2.5-flash-preview-04-17", // Match the model in the PS script URL
        "claude-sonnet-3.7", // Example other models
        "gpt-4o-mini"
    )
    var selectedModel by mutableStateOf(modelOptions[0])
        private set

    // State for the language tabs
    val languageTabs = listOf("EN", "JA", "ZH")
    var selectedLanguageTabIndex by mutableStateOf(0)

    // State for loading indicator
    var isLoading by mutableStateOf(false)

    // State for fetched data
    var fetchedResult: WordFetchResultJson? by mutableStateOf(null)

    // State for error message (null means no error)
    var errorMessage: String? by mutableStateOf(null)

    // --- Event Handlers ---

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun onModelSelect(model: String) {
        selectedModel = model
        // TODO: If the API URL changes based on model, update the API client or logic here
        // For now, the API client is hardcoded to gemini-2.5-flash-preview-04-17
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

        logger.info { "Fetching word details for query: '$searchQuery'" } // Replaced println
        isLoading = true
        errorMessage = null // Clear previous errors
        fetchedResult = null // Clear previous results

        viewModelScope.launch {
            try {
                // Call the API using the current search query
                val result = api.fetchWordDetails(searchQuery)
                fetchedResult = result
                logger.info { "Successfully fetched word details for '$searchQuery'" } // Replaced println
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
        selectedModel = modelOptions[0]
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
