package com.crossevol.wordbook.data

import com.crossevol.wordbook.db.AppDatabase
import com.crossevol.wordbook.ui.screens.ApiKeyConfig as UiApiKeyConfig // Alias your UI model
import io.github.oshai.kotlinlogging.KotlinLogging // Import KotlinLogging

private val logger = KotlinLogging.logger {} // Add logger instance

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
        logger.debug { "Fetching all API key configurations." } // Replaced println
        return apiKeyConfigQueries.selectAll().executeAsList().map { dbConfig ->
            // Map the generated database entity to your UI model
            UiApiKeyConfig(
                id = dbConfig.id,
                alias = dbConfig.alias,
                apiKey = dbConfig.apiKey, // Be cautious with exposing API keys
                provider = dbConfig.provider,
                model = dbConfig.model
            )
        }.also {
            logger.debug { "Fetched ${it.size} API key configurations." } // Replaced println
        }
    }

    /**
     * Get an API key configuration by its ID.
     */
    fun getApiKeyConfigById(id: Long): UiApiKeyConfig? {
        logger.debug { "Fetching API key configuration by ID: $id" } // Replaced println
        return apiKeyConfigQueries.selectById(id).executeAsOneOrNull()?.let { dbConfig ->
            UiApiKeyConfig(
                id = dbConfig.id,
                alias = dbConfig.alias,
                apiKey = dbConfig.apiKey,
                provider = dbConfig.provider,
                model = dbConfig.model
            )
        }.also {
            if (it != null) {
                logger.debug { "Found API key configuration for ID: $id" } // Replaced println
            } else {
                logger.warn { "No API key configuration found for ID: $id" } // Replaced println
            }
        }
    }

    /**
     * Insert a new API key configuration.
     */
    fun insertApiKeyConfig(config: UiApiKeyConfig) {
        logger.info { "Inserting new API key configuration: ${config.alias}" } // Replaced println
        apiKeyConfigQueries.insertConfig(
            alias = config.alias,
            apiKey = config.apiKey,
            provider = config.provider,
            model = config.model
        )
        logger.debug { "Insert successful for API key: ${config.alias}" } // Replaced println
    }

    /**
     * Update an existing API key configuration.
     */
    fun updateApiKeyConfig(config: UiApiKeyConfig) {
        // Ensure the config has a valid ID for update
        if (config.id == 0L) {
            logger.error { "Cannot update config without a valid ID." } // Replaced println
            return
        }
        logger.info { "Updating API key configuration: ${config.alias} (ID: ${config.id})" } // Replaced println
        apiKeyConfigQueries.updateConfig(
            alias = config.alias,
            apiKey = config.apiKey,
            provider = config.provider,
            model = config.model,
            id = config.id
        )
        logger.debug { "Update successful for API key: ${config.alias} (ID: ${config.id})" } // Replaced println
    }

    /**
     * Delete an API key configuration by its ID.
     */
    fun deleteApiKeyConfigById(id: Long) {
        logger.info { "Deleting API key configuration by ID: $id" } // Replaced println
        apiKeyConfigQueries.deleteById(id)
        logger.debug { "Delete successful for API key ID: $id" } // Replaced println
    }

    /**
     * Count the number of API key configurations in the database.
     */
    fun countConfigs(): Long {
        val count = apiKeyConfigQueries.countConfigs().executeAsOne()
        logger.debug { "Counted $count API key configurations." } // Replaced println
        return count
    }

    // TODO: Add method to get the currently selected/active API key config
}
