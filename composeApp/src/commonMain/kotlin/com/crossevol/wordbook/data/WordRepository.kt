package com.crossevol.wordbook.data

import com.crossevol.wordbook.data.model.WordItem
import com.crossevol.wordbook.db.AppDatabase

/**
 * Repository class for interacting with the word and wordDetail tables in the database.
 *
 * This is a basic stub. You will need to implement full CRUD operations
 * and mapping logic between database entities and your UI models (like WordItem).
 */
class WordRepository(private val database: AppDatabase) {

    private val wordQueries = database.wordQueries
    private val wordDetailQueries = database.wordDetailQueries

    /**
     * Example: Get all words and their English details.
     * This is a simplified example and assumes English details exist.
     * A real implementation would handle missing details and potentially
     * fetch details for a configurable language.
     */
    fun getAllWordsWithEnglishDetails(): List<WordItem> {
        // This requires a join or fetching separately and combining.
        // SQLDelight supports joins in .sq files for more efficient queries.
        // For simplicity here, we'll fetch words and then details (less efficient).

        val words = wordQueries.selectAll().executeAsList()
        val wordItems = mutableListOf<WordItem>()

        words.forEach { dbWord ->
            val englishDetail = wordDetailQueries.selectDetailForWordAndLanguage(
                word_id = dbWord.id,
                language_code = "en" // Assuming 'en' for English
            ).executeAsOneOrNull() // Get the single English detail or null

            // Map database entities to your UI WordItem model
            val wordItem = WordItem(
                id = dbWord.id,
                title = dbWord.text,
                pronunciation = englishDetail?.pronunciation ?: "", // Handle nullable fields
                explanation = englishDetail?.explanation ?: "",
                rating = englishDetail?.review_progress ?: 0,
                sentences = englishDetail?.sentences?.split(";")?.filter { it.isNotBlank() } ?: emptyList(), // Convert String to List
                languageCode = "en", // This WordItem represents English details
                wordDetailId = englishDetail?.id
            )
            wordItems.add(wordItem)
        }

        return wordItems
    }

    /**
     * Example: Insert a new word and its details for a specific language.
     * This is a simplified example. A real implementation might first check
     * if the word exists, insert it if not, and then insert/update the detail.
     */
    fun insertWordWithDetail(wordItem: WordItem) {
        database.transaction { // Use transaction for multiple inserts/updates
            // Insert the core word. Handle potential UNIQUE constraint violation if word already exists.
            // A better approach might be to select by text first.
            wordQueries.insertWord(
                text = wordItem.title,
                create_at = System.currentTimeMillis() // Use current time
            )

            // Get the ID of the newly inserted word
            val wordId = wordQueries.selectByText(wordItem.title).executeAsOne().id

            // Insert the word detail for the specific language
            wordDetailQueries.insertDetail(
                word_id = wordId,
                language_code = wordItem.languageCode,
                explanation = wordItem.explanation,
                sentences = wordItem.sentences.joinToString(";"), // Convert List to String
                related_words = null, // Assuming related_words is not in WordItem yet
                pronunciation = wordItem.pronunciation,
                last_review_at = null, // Set initial review time
                review_progress = wordItem.rating
            )
        }
    }

    // TODO: Add methods for:
    // - getWordById(id: Long): dbWord?
    // - getWordDetailById(id: Long): wordDetail?
    // - getWordDetailForWordAndLanguage(wordId: Long, languageCode: String): wordDetail?
    // - updateWordDetail(wordDetail: wordDetail)
    // - deleteWordById(id: Long) (ON DELETE CASCADE handles details)
    // - deleteWordDetailById(id: Long)
    // - Mapping functions (dbWord + wordDetail -> WordItem, WordItem -> dbWord + wordDetail)
}
