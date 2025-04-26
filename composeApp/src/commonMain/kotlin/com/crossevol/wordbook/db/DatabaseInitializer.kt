package com.crossevol.wordbook.db

import com.crossevol.wordbook.data.ApiKeyConfigRepository
import com.crossevol.wordbook.ui.screens.ApiKeyConfig
import com.crossevol.wordbook.data.WordRepository // Import WordRepository
// Import the sample lists (adjust path if necessary, assuming they are accessible)
import com.crossevol.wordbook.ui.screens.sampleWordListEN
import com.crossevol.wordbook.ui.screens.sampleWordListJA
import com.crossevol.wordbook.ui.screens.sampleWordListZH
import io.github.oshai.kotlinlogging.KotlinLogging // Import KotlinLogging

private val logger = KotlinLogging.logger {} // Add logger instance

/**
 * Handles initial database setup, like populating tables with default data if empty.
 */
fun initializeDatabase(
    database: AppDatabase,
    apiKeyConfigRepository: ApiKeyConfigRepository,
    wordRepository: WordRepository // Add WordRepository parameter
) {
    // Check if the apiKeyConfig table is empty
    if (apiKeyConfigRepository.countConfigs() == 0L) {
        logger.info { "DatabaseInitializer: apiKeyConfig table is empty, inserting dummy data." } // Replaced println
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
        logger.info { "DatabaseInitializer: Dummy data inserted." } // Replaced println
    } else {
        logger.debug { "DatabaseInitializer: apiKeyConfig table is not empty, skipping dummy data insertion." } // Replaced println
    }

    // --- Seed Word Data ---
    // Check if the word table is empty using the new query
    val wordCount = database.wordQueries.countWords().executeAsOne()
    if (wordCount == 0L) {
        logger.info { "DatabaseInitializer: word table is empty, inserting sample word data." }
        database.transaction { // Use a transaction for efficiency
            // Seed English words
            sampleWordListEN.forEach { item ->
                wordRepository.saveWordDetails(
                    title = item.title,
                    languageCode = "EN",
                    explanation = item.explanation,
                    sentences = item.sentences,
                    pronunciation = item.pronunciation,
                    relatedWords = item.relatedWords,
                    rating = item.rating
                )
            }
            // Seed Japanese words
            sampleWordListJA.forEach { item ->
                wordRepository.saveWordDetails(
                    title = item.title,
                    languageCode = "JA",
                    explanation = item.explanation,
                    sentences = item.sentences,
                    pronunciation = item.pronunciation,
                    relatedWords = item.relatedWords,
                    rating = item.rating
                )
            }
            // Seed Chinese words
            sampleWordListZH.forEach { item ->
                wordRepository.saveWordDetails( /* ... same pattern as above ... */ title = item.title, languageCode = "ZH", explanation = item.explanation, sentences = item.sentences, pronunciation = item.pronunciation, relatedWords = item.relatedWords, rating = item.rating)
            }
        }
        logger.info { "DatabaseInitializer: Sample word data inserted." }
    } else {
        logger.debug { "DatabaseInitializer: word table is not empty, skipping sample word data insertion." }
    }
}
