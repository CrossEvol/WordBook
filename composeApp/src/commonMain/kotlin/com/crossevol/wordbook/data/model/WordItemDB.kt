package com.crossevol.wordbook.data.model

/**
 * Represents the data required to display a word item in the list and for repository operations.
 *
 * @property id Unique identifier for the core word item.
 * @property title The main word or phrase.
 * @property pronunciation Optional alternative text or pronunciation guide (e.g., "[ Hello World ]").
 * @property explanation Optional explanation or definition.
 * @property rating A numerical rating, typically representing review progress (e.g., 0-5).
 * @property sentences A list of example sentences or usage contexts.
 * @property languageCode The language code for the details (e.g., "en", "ja", "zh").
 * @property wordDetailId Unique identifier for the specific word detail entry (can be null if detail doesn't exist).
 */
data class WordItemDB(
    val id: Long, // Assuming a Long ID from the database (core word ID)
    val title: String,
    val pronunciation: String,
    val explanation: String,
    val rating: Long,
    val sentences: List<String>,
    val languageCode: String, // Add language code
    val wordDetailId: Long? = null // Add optional word detail ID
)
