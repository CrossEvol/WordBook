package com.crossevol.wordbook.data

// Removed okio and java.io imports
// Removed java.text and java.util imports for Date/Locale/SimpleDateFormat
import com.crossevol.wordbook.data.model.LanguageCode
import com.crossevol.wordbook.data.model.WordExportJson
import com.crossevol.wordbook.data.model.WordItemUI
import com.crossevol.wordbook.db.AppDatabase
import com.crossevol.wordbook.db.SelectWordItemsForLanguage
import com.crossevol.wordbook.util.ReviewCalculator
import com.crossevol.wordbook.writeToFile
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

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
            relatedWords = this.related_words?.split(";")?.filter { it.isNotBlank() }
                ?: emptyList() // Split related_words string
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
     * Get all words that need review (their next_review_at time is before current time).
     *
     * @param languageCode The language code to filter by.
     * @return A list of WordItemUI objects that need review.
     */
    fun getWordsNeedingReview(languageCode: String): List<WordItemUI> {
        val currentTime = System.currentTimeMillis()
        return wordDetailQueries.selectWordItemsForLanguage(languageCode)
            .executeAsList()
            .map { it.toUiWordItem() }
            .filter { wordItem ->
                // Get the word to check its next_review_at time
                val word = wordQueries.selectById(wordItem.id).executeAsOneOrNull()
                // Include words where next_review_at is before or equal to current time
                // (meaning it's due for review) or if next_review_at is null
                word?.next_review_at?.let { it <= currentTime } ?: true
            }
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

            // Calculate next review time based on rating/progress using the utility class
            val nextReviewAt = ReviewCalculator.calculateNextReviewTime(rating)

            // Check if the word already exists
            val existingWord = wordQueries.selectByText(title).executeAsOneOrNull()

            val wordId = if (existingWord == null) {
                // Insert the core word if it doesn't exist
                // For new words, last_review_at is initially set to null
                wordQueries.insertWord(
                    text = title,
                    create_at = currentTime,
                    last_review_at = null, // Initial last_review_at is null for new words
                    next_review_at = nextReviewAt, // Set calculated next review time
                    review_progress = rating // Save rating on the word table
                )
                // Get the ID of the newly inserted word
                wordQueries.selectByText(title).executeAsOne().id
            } else {
                // Use the ID of the existing word
                val existingWordId = existingWord.id

                // Get the current next_review_at as the new last_review_at
                val previousNextReviewAt = existingWord.next_review_at

                // Update the last_review_at to previous next_review_at (if it wasn't null)
                wordQueries.updateLastReviewTime(
                    last_review_at = previousNextReviewAt ?: currentTime,
                    id = existingWordId
                )

                // Update next_review_at based on new review progress
                wordQueries.updateNextReviewTime(
                    next_review_at = nextReviewAt,
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

    /**
     * Updates a word's review progress and calculates the next review time.
     *
     * @param wordId The ID of the word to update.
     * @param newProgress The new review progress value (0-7).
     * @return True if update was successful, false otherwise.
     */
    fun updateWordReviewProgress(
        wordId: Long,
        newProgress: Long
    ): Boolean {
        return try {
            database.transaction {
                // Get current word data
                val word = wordQueries.selectById(wordId).executeAsOneOrNull() ?: return@transaction

                // Calculate next review time based on new progress using the utility class
                val nextReviewAt = ReviewCalculator.calculateNextReviewTime(newProgress)

                // Update last_review_at to current next_review_at (if it exists)
                wordQueries.updateLastReviewTime(
                    last_review_at = word.next_review_at ?: System.currentTimeMillis(),
                    id = wordId
                )

                // Update next_review_at
                wordQueries.updateNextReviewTime(
                    next_review_at = nextReviewAt,
                    id = wordId
                )

                // Update review_progress
                wordQueries.updateReviewProgress(
                    review_progress = newProgress,
                    id = wordId
                )
            }
            true
        } catch (e: Exception) {
            logger.error(e) { "Error updating word review progress: ${e.message}" }
            false
        }
    }

    /**
     * Updates a word's review progress based on whether it was remembered or forgotten.
     *
     * @param wordId The ID of the word to update
     * @param remembered Whether the user remembered the word
     * @return True if the update was successful, false otherwise
     */
    fun updateWordReviewResult(
        wordId: Long,
        remembered: Boolean
    ): Boolean {
        return try {
            database.transaction {
                // Get current word data
                val word = wordQueries.selectById(wordId).executeAsOneOrNull() ?: return@transaction

                // Calculate new progress and next review time based on current progress and result
                val (newProgress, nextReviewAt) = ReviewCalculator.calculateReviewUpdate(
                    word.review_progress,
                    remembered
                )

                // Update last_review_at to current time
                wordQueries.updateLastReviewTime(
                    last_review_at = System.currentTimeMillis(),
                    id = wordId
                )

                // Update next_review_at
                wordQueries.updateNextReviewTime(
                    next_review_at = nextReviewAt,
                    id = wordId
                )

                // Update review_progress
                wordQueries.updateReviewProgress(
                    review_progress = newProgress,
                    id = wordId
                )
            }
            true
        } catch (e: Exception) {
            logger.error(e) { "Error updating word review result: ${e.message}" }
            false
        }
    }

    /**
     * Export all words with details in all languages. It prepares the content
     * and calls the platform-specific `writeToFile` function.
     *
     * @param directoryLocation The target directory path (Desktop) or URI string (Android).
     * @param format The format to export as ("JSON" or "CSV").
     * @return The path/URI string of the exported file, or null if export failed.
     */
    fun exportWords(
        directoryLocation: String,
        format: String
    ): String? {
        try {
            // Get all words from the database
            val allWords = wordQueries.selectAll().executeAsList()

            // Create the export data for each word
            val exportData = allWords.map { word ->
                // Get details for each language
                val enDetail = wordDetailQueries.selectDetailForWordAndLanguage(
                    word.id,
                    LanguageCode.EN.name
                ).executeAsOneOrNull()
                val jaDetail = wordDetailQueries.selectDetailForWordAndLanguage(
                    word.id,
                    LanguageCode.JA.name
                ).executeAsOneOrNull()
                val zhDetail = wordDetailQueries.selectDetailForWordAndLanguage(
                    word.id,
                    LanguageCode.ZH.name
                ).executeAsOneOrNull()

                // Create export model with defaults for null values
                WordExportJson(
                    text = word.text,
                    lastReviewAt = word.last_review_at ?: 0L, // Use Long, remove .toInt()
                    nextReviewAt = word.next_review_at ?: 0L, // Use Long, remove .toInt()
                    reviewProgress = word.review_progress.toInt(), // Assuming review_progress fits Int

                    // English details
                    enExplanation = enDetail?.explanation ?: "",
                    enSentences = enDetail?.sentences ?: "",
                    enRelatedWords = enDetail?.related_words ?: "",
                    enPronunciation = enDetail?.pronunciation ?: "",

                    // Japanese details
                    jaExplanation = jaDetail?.explanation ?: "",
                    jaSentences = jaDetail?.sentences ?: "",
                    jaRelatedWords = jaDetail?.related_words ?: "",
                    jaPronunciation = jaDetail?.pronunciation ?: "",

                    // Chinese details
                    zhExplanation = zhDetail?.explanation ?: "",
                    zhSentences = zhDetail?.sentences ?: "",
                    zhRelatedWords = zhDetail?.related_words ?: "",
                    zhPronunciation = zhDetail?.pronunciation ?: ""
                )
            }

            val baseFilename = "wordbook_export"
            val contentString: String
            val fileExtension: String

            // Prepare content string based on format
            when (format.uppercase()) {
                "JSON" -> {
                    val json = Json { prettyPrint = true }
                    contentString = json.encodeToString(exportData)
                    fileExtension = "json"
                }

                "CSV"  -> {
                    // Use StringBuilder for manual CSV creation to avoid external library dependency here if not needed elsewhere
                    // Or keep kotlincsv if it's used elsewhere or preferred
                    val csvBuilder = StringBuilder()
                    // Write header
                    csvBuilder.appendLine(
                        listOf(
                            "text",
                            "lastReviewAt",
                            "nextReviewAt",
                            "reviewProgress",
                            "enExplanation",
                            "enSentences",
                            "enRelatedWords",
                            "enPronunciation",
                            "jaExplanation",
                            "jaSentences",
                            "jaRelatedWords",
                            "jaPronunciation",
                            "zhExplanation",
                            "zhSentences",
                            "zhRelatedWords",
                            "zhPronunciation"
                        ).joinToString(",") // Simple comma separation, consider quoting if values might contain commas
                    )
                    // Write data rows
                    exportData.forEach { item ->
                        csvBuilder.appendLine(
                            listOf(
                                item.text,
                                item.lastReviewAt.toString(),
                                item.nextReviewAt.toString(),
                                item.reviewProgress.toString(),
                                item.enExplanation,
                                item.enSentences,
                                item.enRelatedWords,
                                item.enPronunciation,
                                item.jaExplanation,
                                item.jaSentences,
                                item.jaRelatedWords,
                                item.jaPronunciation,
                                item.zhExplanation,
                                item.zhSentences,
                                item.zhRelatedWords,
                                item.zhPronunciation
                            ).joinToString(",") // Simple comma separation
                        )
                    }
                    contentString = csvBuilder.toString()
                    fileExtension = "csv"
                }

                else   -> {
                    logger.error { "Unsupported export format: $format" }
                    return null
                }
            }

            // Call the platform-specific write function
            val finalPath = writeToFile(
                directoryLocation,
                baseFilename,
                fileExtension,
                contentString
            )

            if (finalPath != null) {
                logger.info { "Successfully initiated export of ${exportData.size} words to $finalPath" }
            } else {
                logger.error { "Export failed for ${exportData.size} words." }
            }
            return finalPath // Return the path/URI returned by writeToFile

        } catch (e: Exception) {
            // Log errors occurring during data fetching or content preparation
            logger.error(e) { "Error preparing data for export: ${e.message}" }
            return null
        }
    }

    // TODO: Add methods for:
    // - getWordById(id: Long): word? (SQLDelight generated)
    // - getWordDetailById(id: Long): wordDetail? (SQLDelight generated)
    // - getWordDetailForWordAndLanguage(wordId: Long, languageCode: String): wordDetail? (SQLDelight generated)
    // - deleteWordById(id: Long)
    // - deleteWordDetailById(id: Long)
    // - Mapping functions (SelectWordItemsForLanguage -> UiWordItem) - Added as private extension function
    // - A method to update next_review_at based on spaced repetition logic
}
