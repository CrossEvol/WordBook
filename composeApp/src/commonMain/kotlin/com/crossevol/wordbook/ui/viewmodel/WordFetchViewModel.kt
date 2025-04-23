package com.crossevol.wordbook.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crossevol.wordbook.data.api.WordFetchApi
import com.crossevol.wordbook.data.api.WordFetchResultJson
import kotlinx.coroutines.launch

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
    }

    fun onLanguageTabSelect(index: Int) {
        selectedLanguageTabIndex = index
    }

    fun fetchWord() {
        if (searchQuery.isBlank()) {
            errorMessage = "Please enter a word or phrase to translate."
            return
        }
        if (isLoading) return // Prevent multiple requests

        isLoading = true
        errorMessage = null // Clear previous errors
        fetchedResult = null // Clear previous results

        viewModelScope.launch {
            try {
                // Call the API using the current search query
                val result = api.fetchWordDetails(searchQuery)
                fetchedResult = result
                // Automatically switch to the language tab that matches the input language?
                // Or just stay on the current tab. Let's stay on the current tab for now.

            } catch (e: Exception) {
                println("Fetch Error in ViewModel: ${e.message}")
                errorMessage = "Error fetching data: ${e.message}"
            } finally {
                isLoading = false
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
    }

    fun dismissErrorDialog() {
        errorMessage = null
    }
}
