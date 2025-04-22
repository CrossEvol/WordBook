package com.crossevol.wordbook.data.model

/**
 * Represents the data required to display a word item in the list.
 *
 * @property id Unique identifier for the word item.
 * @property title The main word or phrase.
 * @property explanation Optional alternative text or pronunciation guide (e.g., "[ Hello World ]").
 * @property rating A numerical rating, typically representing review progress (e.g., 0-5).
 * @property sentences A list of example sentences or usage contexts.
 */
data class WordItem(
    val id: Long, // Assuming a Long ID from the database
    val title: String,
    val pronunciation: String,
    val explanation: String,
    val rating: Int,
    val sentences: List<String>
)
