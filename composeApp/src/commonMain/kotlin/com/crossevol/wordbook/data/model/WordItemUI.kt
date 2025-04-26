package com.crossevol.wordbook.data.model

/**
 * Represents the data required to display a word item in the list and detail screens.
 * This model is specifically for the UI layer.
 *
 * @property id Unique identifier for the core word item (maps to word.id).
 * @property title The main word or phrase.
 * @property pronunciation Optional alternative text or pronunciation guide (e.g., "[ Hello World ]").
 * @property explanation Optional explanation or definition.
 * @property rating A numerical rating, typically representing review progress (e.g., 0-5).
 * @property sentences A list of example sentences or usage contexts.
 */
data class WordItemUI(
    val id: Long, // Maps to word.id
    val title: String,
    val pronunciation: String,
    val explanation: String,
    val rating: Long,
    val sentences: List<String>
)
