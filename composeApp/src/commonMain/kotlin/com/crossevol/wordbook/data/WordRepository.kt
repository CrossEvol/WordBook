package com.crossevol.wordbook.data

import com.crossevol.wordbook.data.model.WordItemUI // Import the new UI model
import com.crossevol.wordbook.db.AppDatabase
import com.crossevol.wordbook.db.SelectWordItemsForLanguage // SQLDelight generated class

/**
 * Repository class for interacting with the word and wordDetail tables in the database.
 * Handles mapping between database entities and UI models.
 */
open class WordRepository(private val database: AppDatabase) {

    private val wordQueries = database.wordQueries
    private val wordDetailQueries = database.wordDetailQueries

    /**
     * Maps a SQLDelight generated SelectWordItemsForLanguage object to a UiWordItem.
     * review_progress is now sourced from the 'word' table via the updated SQL query.
     */
    private fun SelectWordItemsForLanguage.toUiWordItem(): WordItemUI {
        return WordItemUI(
            id = this.id, // word.id
            title = this.title, // word.text
            pronunciation = this.pronunciation ?: "", // Handle nullable
            explanation = this.explanation ?: "", // Handle nullable
            rating = this.review_progress, // Maps to review_progress (now from word table)
            sentences = this.sentences?.split(";")?.filter { it.isNotBlank() } ?: emptyList(),
            relatedWords = this.related_words?.split(";")?.filter { it.isNotBlank() } ?: emptyList() // Split related_words string
        )
    }

    /**
     * Get all word items with details for a specific language, mapped for UI display.
     *
     * @param languageCode The language code (e.g., "en", "ja", "zh").
     * @return A list of UiWordItem objects.
     */
    fun getWordItemsForLanguage(languageCode: String): List<WordItemUI> {
        // Use the new SQLDelight query and map results to UiWordItem
        return wordDetailQueries.selectWordItemsForLanguage(languageCode)
            .executeAsList()
            .map { it.toUiWordItem() }
    }

    /**
     * Saves word details for a specific language.
     * Inserts the core word if it doesn't exist, then inserts or updates the word detail.
     * Also updates the last_review_at timestamp and review_progress on the core word entry.
     *
     * @param title The main word or phrase.
     * @param languageCode The language code for the details.
     * @param explanation Optional explanation.
     * @param sentences List of example sentences.
     * @param pronunciation Optional pronunciation guide.
     * @param relatedWords List of related words.
     * @param rating Review progress rating (now saved on the word table).
     */
    open fun saveWordDetails(
        title: String,
        languageCode: String,
        explanation: String?,
        sentences: List<String>,
        pronunciation: String?,
        relatedWords: List<String>, // Add relatedWords parameter
        rating: Long // This rating will now be saved on the 'word' table
    ) {
        database.transaction {
            val currentTime = System.currentTimeMillis()

            // Check if the word already exists
            val existingWord = wordQueries.selectByText(title).executeAsOneOrNull()

            val wordId = if (existingWord == null) {
                // Insert the core word if it doesn't exist
                wordQueries.insertWord(
                    text = title,
                    create_at = currentTime,
                    last_review_at = currentTime, // Set initial review time on the word
                    review_progress = rating // Save rating on the word table
                )
                // Get the ID of the newly inserted word
                wordQueries.selectByText(title).executeAsOne().id
            } else {
                // Use the ID of the existing word
                val existingWordId = existingWord.id
                // Update the last_review_at timestamp on the existing word
                wordQueries.updateLastReviewTime(
                    last_review_at = currentTime,
                    id = existingWordId
                )
                // Update the review_progress on the existing word
                wordQueries.updateReviewProgress(
                    review_progress = rating,
                    id = existingWordId
                )
                existingWordId
            }

            // Check if detail for this language already exists for this word
            val existingDetail = wordDetailQueries.selectDetailForWordAndLanguage(
                word_id = wordId,
                language_code = languageCode
            ).executeAsOneOrNull()

            if (existingDetail == null) {
                // Insert new word detail (review_progress is NOT included here)
                wordDetailQueries.insertDetail(
                    word_id = wordId,
                    language_code = languageCode,
                    explanation = explanation,
                    sentences = sentences.joinToString(";"), // Convert List to String
                    related_words = relatedWords.joinToString(";"), // Convert List to String
                    pronunciation = pronunciation
                    // review_progress is no longer in wordDetail
                )
            } else {
                // Update existing word detail (review_progress is NOT included here)
                wordDetailQueries.updateDetail(
                    explanation = explanation,
                    sentences = sentences.joinToString(";"),
                    related_words = relatedWords.joinToString(";"), // Use the new relatedWords parameter
                    pronunciation = pronunciation,
                    id = existingDetail.id // Update by detail ID
                    // review_progress is no longer in wordDetail
                )
            }
        }
    }

    // The old insertWordWithDetail method is replaced by saveWordDetails
    // You can remove or refactor the old method if it's no longer needed.
    // For now, I've replaced its logic with the new saveWordDetails function.

    // TODO: Add methods for:
    // - getWordById(id: Long): word? (SQLDelight generated)
    // - getWordDetailById(id: Long): wordDetail? (SQLDelight generated)
    // - getWordDetailForWordAndLanguage(wordId: Long, languageCode: String): wordDetail? (SQLDelight generated)
    // - deleteWordById(id: Long)
    // - deleteWordDetailById(id: Long)
    // - Mapping functions (SelectWordItemsForLanguage -> UiWordItem) - Added as private extension function
}
