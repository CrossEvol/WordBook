package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Divider // Use Material3 Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.model.FilterOption // Import FilterOption
import com.crossevol.wordbook.data.model.WordItemUI // Import the new UI model
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Displays a simple star rating bar using Material 3 Icons.
 *
 * @param rating The current rating (number of filled stars).
 * @param maxRating The maximum possible rating (total number of stars).
 * @param modifier Modifier for this composable.
 * @param starColor The color of the filled stars.
 * @param emptyStarColor The color of the empty stars (outlines).
 */
@Composable
fun RatingBar(
    rating: Long,
    maxRating: Int = 5,
    modifier: Modifier = Modifier,
    starColor: Color = Color(0xFFFFC107), // Amber color for filled stars
    emptyStarColor: Color = MaterialTheme.colorScheme.outline // Use outline color for empty
) {
    Row(modifier = modifier) {
        for (i in 1..maxRating) {
            Icon(
                // Use filled star if i <= rating, otherwise use outlined star
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (i <= rating) "Filled Star $i" else "Empty Star $i",
                tint = if (i <= rating) starColor else emptyStarColor,
                modifier = Modifier.size(18.dp) // Adjust size as needed
            )
        }
    }
}


/**
 * A composable function that displays a single word item based on the design.
 * Uses Material 3 components.
 *
 * @param item The [WordItemUI] data to display. // Changed from WordItem
 * @param visibleFilters A set of [FilterOption] indicating which parts should be visible.
 * @param modifier Modifier for this composable.
 */
@Composable
fun WordListItem(
    item: WordItemUI, // Changed from WordItem
    visibleFilters: Set<FilterOption>,
    modifier: Modifier = Modifier
) {
    // Determine visibility based on filters
    val showAll = visibleFilters.contains(FilterOption.ALL)
    val showNone = visibleFilters.contains(FilterOption.NONE)

    val showPronunciation =
        showAll || (!showNone && visibleFilters.contains(FilterOption.PRONUNCIATION))
    val showExplanation =
        showAll || (!showNone && visibleFilters.contains(FilterOption.EXPLANATION))
    val showProgress = showAll || (!showNone && visibleFilters.contains(FilterOption.PROGRESS))
    val showSentences = showAll || (!showNone && visibleFilters.contains(FilterOption.SENTENCE))

    Column(
        modifier = modifier.fillMaxWidth().padding(
            vertical = 8.dp,
            horizontal = 16.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            // Removed SpaceBetween to keep rating closer if altText is missing
        ) {
            // Left part: Title and AltText
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.weight(1f)// Take available space
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                // Conditionally show Pronunciation
                if (showPronunciation && item.pronunciation.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.pronunciation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp) // Align baseline better
                    )
                }
            }
            // Conditionally show RatingBar (Progress)
            if (showProgress) {
                RatingBar(
                    rating = item.rating,
                    maxRating = 5
                )
            }
        }

        // Conditionally show Explanation
        if (showExplanation && item.explanation.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp)) // Add space if showing explanation
            Text(
                text = item.explanation,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Conditionally show Sentences (Moved outside explanation block)
        if (showSentences && item.sentences.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            // Examples section
            Column { // Use Column to stack examples
                item.sentences.forEach { example ->
                    Text(
                        text = example,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        // Only show divider if not in NONE mode (Moved outside explanation block)
        // This ensures the divider appears if *any* content below the title row is shown.
        if (!showNone && (showPronunciation || showExplanation || showProgress || showSentences)) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant) // Use Material3 Divider
        }
    }
}

// Update sample data to use UiWordItem
val sampleWordItem = WordItemUI( // Changed from WordItem
    id = 1L,
    title = "Hello World",
    explanation = "Say welcome to the world.",
    pronunciation = "[ Nǐ hǎo shìjiè ]",
    rating = 3,
    sentences = listOf(
        "The bright sun shines over the vast ocean, casting a golden glow on the waves today.",
        "The bright sun shines over the vast ocean, casting a golden glow on the waves today.",
    ),
)

@Preview
@Composable
fun WordListItemPreview() {
    MaterialTheme { // Wrap with MaterialTheme for preview
        Surface { // Use Surface for background color
            WordListItem(
                item = sampleWordItem,
                visibleFilters = setOf()
            )
        }
    }
}

@Preview
@Composable
fun WordListItemPreview_NoAltText() {
    MaterialTheme {
        Surface {
            WordListItem(
                item = sampleWordItem.copy(
                    explanation = "Say welcome",
                    rating = 1
                ),
                visibleFilters = setOf()
            )
        }
    }
}

@Preview
@Composable
fun WordListItemPreview_FullRating() {
    MaterialTheme {
        Surface {
            WordListItem(
                item = sampleWordItem.copy(rating = 5),
                visibleFilters = setOf()
            )
        }
    }
}

@Preview
@Composable
fun WordListItemPreview_ZeroRating() {
    MaterialTheme {
        Surface {
            WordListItem(
                item = sampleWordItem.copy(rating = 0),
                visibleFilters = setOf()
            )
        }
    }
}
