package com.crossevol.wordbook.data

import com.crossevol.wordbook.db.AppDatabase
import com.crossevol.wordbook.ui.screens.ApiKeyConfig as UiApiKeyConfig // Alias your UI model

/**
 * Repository class for interacting with the apiKeyConfig table.
 *
 * This is a basic stub. You will need to implement full CRUD operations
 * and mapping logic between database entities and your UI models (like ApiKeyConfig).
 */
class ApiKeyConfigRepository(private val database: AppDatabase) {

    private val apiKeyConfigQueries = database.apiKeyConfigQueries

    /**
     * Get all API key configurations.
     */
    fun getAllApiKeyConfigs(): List<UiApiKeyConfig> {
        return apiKeyConfigQueries.selectAll().executeAsList().map { dbConfig ->
            // Map the generated database entity to your UI model
            UiApiKeyConfig(
                id = dbConfig.id,
                alias = dbConfig.alias,
                apiKey = dbConfig.apiKey, // Be cautious with exposing API keys
                provider = dbConfig.provider,
                model = dbConfig.model
            )
        }
    }

    /**
     * Get an API key configuration by its ID.
     */
    fun getApiKeyConfigById(id: Long): UiApiKeyConfig? {
        return apiKeyConfigQueries.selectById(id).executeAsOneOrNull()?.let { dbConfig ->
            UiApiKeyConfig(
                id = dbConfig.id,
                alias = dbConfig.alias,
                apiKey = dbConfig.apiKey,
                provider = dbConfig.provider,
                model = dbConfig.model
            )
        }
    }

    /**
     * Insert a new API key configuration.
     */
    fun insertApiKeyConfig(config: UiApiKeyConfig) {
        apiKeyConfigQueries.insertConfig(
            alias = config.alias,
            apiKey = config.apiKey,
            provider = config.provider,
            model = config.model
        )
    }

    /**
     * Update an existing API key configuration.
     */
    fun updateApiKeyConfig(config: UiApiKeyConfig) {
        // Ensure the config has a valid ID for update
        if (config.id == 0L) {
            // Handle error: Cannot update config without an ID
            return
        }
        apiKeyConfigQueries.updateConfig(
            alias = config.alias,
            apiKey = config.apiKey,
            provider = config.provider,
            model = config.model,
            id = config.id
        )
    }

    /**
     * Delete an API key configuration by its ID.
     */
    fun deleteApiKeyConfigById(id: Long) {
        apiKeyConfigQueries.deleteById(id)
    }

    // TODO: Add method to get the currently selected/active API key config
}
