package com.crossevol.wordbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.crossevol.wordbook.data.ApiKeyConfigRepository
import com.crossevol.wordbook.ui.screens.ApiKeyConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope

private val logger = KotlinLogging.logger {}

/**
 * ViewModel for managing API Key configurations.
 * Handles fetching, saving (add/edit), and deleting API keys.
 */
class ApiKeyViewModel(
    private val repository: ApiKeyConfigRepository
) : ViewModel() {

    // StateFlow to hold the list of API key configurations
    private val _apiKeyConfigs = MutableStateFlow<List<ApiKeyConfig>>(emptyList())
    val apiKeyConfigs: StateFlow<List<ApiKeyConfig>> = _apiKeyConfigs.asStateFlow()

    init {
        // Load initial data when the ViewModel is created
        loadApiKeyConfigs()
    }

    /**
     * Loads all API key configurations from the repository.
     */
    fun loadApiKeyConfigs() { // Made public so it can be called externally if needed
        viewModelScope.launch {
            logger.debug { "Loading API key configurations..." }
            try {
                // Perform database operation on a background thread
                val configs = withContext(Dispatchers.Default) {
                    repository.getAllApiKeyConfigs()
                }
                _apiKeyConfigs.value = configs
                logger.debug { "Successfully loaded ${configs.size} API key configurations." }
            } catch (e: Exception) {
                logger.error(e) { "Error loading API key configurations." }
                // Handle error state if necessary (e.g., show a message)
            }
        }
    }

    /**
     * Saves an API key configuration. Inserts if ID is 0L, updates otherwise.
     *
     * @param config The ApiKeyConfig to save.
     */
    fun saveApiKeyConfig(config: ApiKeyConfig) {
        viewModelScope.launch {
            logger.info { "Saving API key configuration: ${config.alias} (ID: ${config.id})" }
            try {
                withContext(Dispatchers.Default) {
                    if (config.id == 0L) {
                        repository.insertApiKeyConfig(config)
                        logger.info { "Inserted new API Key: ${config.alias}" }
                    } else {
                        repository.updateApiKeyConfig(config)
                        logger.info { "Updated API Key: ${config.alias} (ID: ${config.id})" }
                    }
                }
                // Reload the list after saving to update the UI and other ViewModels observing this state
                loadApiKeyConfigs()
            } catch (e: Exception) {
                logger.error(e) { "Error saving API key configuration: ${config.alias}" }
                // Handle error state
            }
        }
    }

    /**
     * Deletes an API key configuration by its ID.
     *
     * @param config The ApiKeyConfig to delete.
     */
    fun deleteApiKeyConfig(config: ApiKeyConfig) {
        viewModelScope.launch {
            logger.info { "Deleting API key configuration: ${config.alias} (ID: ${config.id})" }
            try {
                withContext(Dispatchers.Default) {
                    repository.deleteApiKeyConfigById(config.id)
                }
                logger.debug { "Delete successful for API key ID: ${config.id}" }
                // Reload the list after deleting to update the UI and other ViewModels observing this state
                loadApiKeyConfigs()
            } catch (e: Exception) {
                logger.error(e) { "Error deleting API key configuration: ${config.alias}" }
                // Handle error state
            }
        }
    }

    // You might add other functions here, e.g., to select an active API key
}
