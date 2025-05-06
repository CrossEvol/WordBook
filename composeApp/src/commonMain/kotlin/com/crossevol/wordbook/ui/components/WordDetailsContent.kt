package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.model.WordItemUI

/**
 * Reusable composable to display the core content of a word's details.
 * Includes title, pronunciation, explanation, sentences, and related words.
 *
 * @param wordItem The WordItemUI data to display.
 * @param modifier Modifier for the root Column.
 * @param onSentenceClick Callback when a sentence item is clicked.
 * @param onRelatedWordClick Callback when a related word item is clicked.
 */
@Composable
fun WordDetailsContent(
    wordItem: WordItemUI,
    modifier: Modifier = Modifier,
    onSentenceClick: (String) -> Unit = {}, // Default to empty lambda (no action)
    onRelatedWordClick: (String) -> Unit = {} // Default to empty lambda (no action)
) {
    Column(
        modifier = modifier
    ) {
        // Word Name (Title)
        Text(
            text = wordItem.title,
            style = MaterialTheme.typography.h5, // Larger headline
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Explanation Label
        Text(
            text = "Pronunciation", // "explanation" in design, capitalized here
            style = MaterialTheme.typography.subtitle2, // Smaller title for label
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Pronunciation (like "[ Hello, world ]")
        Text(
            text = wordItem.pronunciation,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Explanation Label
        Text(
            text = "Explanation", // "explanation" in design, capitalized here
            style = MaterialTheme.typography.subtitle1, // Smaller title for label
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Explanation Text
        Text(
            text = wordItem.explanation,
            style = MaterialTheme.typography.caption
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Sentences Label
        Text(
            text = "Sentences",
            style = MaterialTheme.typography.subtitle1, // Slightly larger title for section
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Sentences List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            wordItem.sentences.forEach { sentence ->
                SentenceItem(text = sentence) {
                    onSentenceClick(sentence) // Use the provided callback
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp)) // Add space before Related Words

        // Related Words Label
        Text(
            text = "Related Words",
            style = MaterialTheme.typography.subtitle1, // Match Sentences label style
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Related Words List
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // Slightly less space than sentences
            wordItem.relatedWords.forEach { relatedWord ->
                RelatedWordItem(text = relatedWord) {
                    onRelatedWordClick(relatedWord) // Use the provided callback
                }
            }
        }
    }
}
