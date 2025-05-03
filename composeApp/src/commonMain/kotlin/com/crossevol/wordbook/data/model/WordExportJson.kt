package com.crossevol.wordbook.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordExportJson(
    val text: String,
    @SerialName("last_review_at") val lastReviewAt: Long, // Changed Int to Long
    @SerialName("next_review_at") val nextReviewAt: Long, // Changed Int to Long
    @SerialName("review_progress") val reviewProgress: Int, // Keep as Int if review_progress fits
    @SerialName("en_explanation") val enExplanation: String,
    @SerialName("en_sentences") val enSentences: String, // Sentences are semicolon-separated
    @SerialName("en_related_words") val enRelatedWords: String, // Words are semicolon-separated
    @SerialName("en_pronunciation") val enPronunciation: String,
    @SerialName("ja_explanation") val jaExplanation: String,
    @SerialName("ja_sentences") val jaSentences: String,
    @SerialName("ja_related_words") val jaRelatedWords: String,
    @SerialName("ja_pronunciation") val jaPronunciation: String,
    @SerialName("zh_explanation") val zhExplanation: String,
    @SerialName("zh_sentences") val zhSentences: String,
    @SerialName("zh_related_words") val zhRelatedWords: String,
    @SerialName("zh_pronunciation") val zhPronunciation: String,
)
