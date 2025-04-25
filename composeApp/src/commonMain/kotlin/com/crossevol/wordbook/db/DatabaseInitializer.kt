package com.crossevol.wordbook.db

import com.crossevol.wordbook.data.ApiKeyConfigRepository
import com.crossevol.wordbook.ui.screens.ApiKeyConfig

/**
 * Handles initial database setup, like populating tables with default data if empty.
 */
fun initializeDatabase(
    database: AppDatabase,
    apiKeyConfigRepository: ApiKeyConfigRepository
) {
    // Check if the apiKeyConfig table is empty
    if (apiKeyConfigRepository.countConfigs() == 0L) {
        println("DatabaseInitializer: apiKeyConfig table is empty, inserting dummy data.")
        // Insert dummy data
        val dummyConfigs = listOf(
            ApiKeyConfig(
                alias = "My Gemini Key",
                apiKey = "YOUR_GEMINI_API_KEY", // Placeholder - replace or handle securely
                provider = "Google",
                model = "gemini-2.5-flash-preview-04-17"
            ),
            ApiKeyConfig(
                alias = "My Claude Key",
                apiKey = "YOUR_CLAUDE_API_KEY", // Placeholder - replace or handle securely
                provider = "Anthropic",
                model = "claude-3-sonnet-20240229"
            ),
            ApiKeyConfig(
                alias = "My OpenAI Key",
                apiKey = "YOUR_OPENAI_API_KEY", // Placeholder - replace or handle securely
                provider = "OpenAI",
                model = "gpt-4o"
            ),
        )

        database.transaction { // Use transaction for batch insertion
            dummyConfigs.forEach { config ->
                apiKeyConfigRepository.insertApiKeyConfig(config)
            }
        }
        println("DatabaseInitializer: Dummy data inserted.")
    } else {
        println("DatabaseInitializer: apiKeyConfig table is not empty, skipping dummy data insertion.")
    }
}
