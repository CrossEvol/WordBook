package com.crossevol.wordbook.data

import com.crossevol.wordbook.data.model.WordItemUI // Import the new UI model
import com.crossevol.wordbook.db.AppDatabase
import com.crossevol.wordbook.db.SelectWordItemsForLanguage // SQLDelight generated class

/**
 * Repository class for interacting with the word and wordDetail tables in the database.
 * Handles mapping between database entities and UI models.
 */
class WordRepository(private val database: AppDatabase) {

    private val wordQueries = database.wordQueries
    private val wordDetailQueries = database.wordDetailQueries

    /**
     * Maps a SQLDelight generated SelectWordItemsForLanguage object to a UiWordItem.
     */
    private fun SelectWordItemsForLanguage.toUiWordItem(): WordItemUI {
        return WordItemUI(
            id = this.id, // word.id
            title = this.title, // word.text
            pronunciation = this.pronunciation ?: "", // Handle nullable
            explanation = this.explanation ?: "", // Handle nullable
            rating = this.review_progress, // Maps to review_progress
            sentences = this.sentences?.split(";")?.filter { it.isNotBlank() } ?: emptyList() // Convert String to List
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
     *
     * @param title The main word or phrase.
     * @param languageCode The language code for the details.
     * @param explanation Optional explanation.
     * @param sentences List of example sentences.
     * @param pronunciation Optional pronunciation guide.
     * @param rating Review progress rating.
     */
    fun saveWordDetails(
        title: String,
        languageCode: String,
        explanation: String?,
        sentences: List<String>,
        pronunciation: String?,
        rating: Long
    ) {
        database.transaction {
            // Check if the word already exists
            val existingWord = wordQueries.selectByText(title).executeAsOneOrNull()

            val wordId = if (existingWord == null) {
                // Insert the core word if it doesn't exist
                wordQueries.insertWord(
                    text = title,
                    create_at = System.currentTimeMillis()
                )
                // Get the ID of the newly inserted word
                wordQueries.selectByText(title).executeAsOne().id
            } else {
                // Use the ID of the existing word
                existingWord.id
            }

            // Check if detail for this language already exists for this word
            val existingDetail = wordDetailQueries.selectDetailForWordAndLanguage(
                word_id = wordId,
                language_code = languageCode
            ).executeAsOneOrNull()

            if (existingDetail == null) {
                // Insert new word detail
                wordDetailQueries.insertDetail(
                    word_id = wordId,
                    language_code = languageCode,
                    explanation = explanation,
                    sentences = sentences.joinToString(";"), // Convert List to String
                    related_words = null, // Assuming related_words is not handled here yet
                    pronunciation = pronunciation,
                    last_review_at = System.currentTimeMillis(), // Set initial review time
                    review_progress = rating
                )
            } else {
                // Update existing word detail
                wordDetailQueries.updateDetail(
                    explanation = explanation,
                    sentences = sentences.joinToString(";"),
                    related_words = existingDetail.related_words, // Keep existing related words if not updated
                    pronunciation = pronunciation,
                    last_review_at = System.currentTimeMillis(), // Update review time on save
                    review_progress = rating,
                    id = existingDetail.id // Update by detail ID
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
