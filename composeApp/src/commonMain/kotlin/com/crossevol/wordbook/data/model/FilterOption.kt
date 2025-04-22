package com.crossevol.wordbook.data.model

/**
 * Represents the different parts of a WordItem that can be filtered for display.
 */
enum class FilterOption(val displayName: String) {
    PRONUNCIATION("Pronunciation"),
    EXPLANATION("Explanation"),
    SENTENCE("Sentence"),
    PROGRESS("Progress"), // Represents the RatingBar
    ALL("All"),
    NONE("None")
}
