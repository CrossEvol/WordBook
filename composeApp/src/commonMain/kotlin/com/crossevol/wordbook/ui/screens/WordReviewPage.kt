package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.mock.sampleWordItem
import com.crossevol.wordbook.data.model.WordItemUI
import com.crossevol.wordbook.ui.components.RelatedWordItem // Import RelatedWordItem
import com.crossevol.wordbook.ui.components.SimpleActionSheet // Import the action sheet
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A screen for reviewing a word, showing details with an overlay and action sheet.
 * Based on design images WordDetailReview.png and Action2SheetSimple.png.
 *
 * @param wordItem The WordItem to display for review.
 * @param onAction Callback invoked when any action (Edit, Delete, Cancel) is taken from the sheet.
 *                 This should typically handle dismissing the review overlay/state.
 * @param onBack Callback invoked when the back navigation is requested (e.g., from TopAppBar).
 *               This might be the same as onAction or different depending on desired flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordReviewPage(
    wordItem: WordItemUI,
    onAction: () -> Unit, // Single callback for any action sheet dismissal
    onBack: () -> Unit   // Separate callback for explicit back navigation
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Content (Similar to WordDetailPage structure)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("left 3") }, // Title from WordDetailReview.png
                    navigationIcon = {
                        IconButton(onClick = onBack) { // Use the onBack callback here
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant // Subdued title color
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = wordItem.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = wordItem.pronunciation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Explanation",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = wordItem.explanation,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Sentences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    wordItem.sentences.forEach { sentence ->
                        // Reusing SentenceItem structure locally for simplicity
                        SentenceItemReview(text = sentence)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp)) // Add space before Related Words

                // Related Words Label
                Text(
                    text = "Related Words",
                    style = MaterialTheme.typography.titleMedium, // Match Sentences label style
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Related Words List
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // Slightly less space than sentences
                    wordItem.relatedWords.forEach { relatedWord ->
                        // Use the standard RelatedWordItem, clicks are blocked by the overlay
                        RelatedWordItem(text = relatedWord) {
                            // Click handler is present but won't be triggered due to overlay
                        }
                    }
                }
            }
        }

        // 2. Semi-Transparent Overlay
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false, onClick = {}), // Prevent clicks passing through to content below sheet
            color = Color.Black.copy(alpha = 0.3f) // Adjust alpha for desired opacity
        ) {}

        // 3. Action Sheet at the Bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // Align sheet to the bottom
        ) {
            SimpleActionSheet(
                onEdit = onAction,   // All buttons trigger the same onAction callback
                onDelete = onAction,
                onCancel = onAction,
                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp) // Add padding around sheet
            )
        }
    }
}

// Simplified Sentence Item for Review Page (non-clickable, matches visual)
@Composable
private fun SentenceItemReview(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null, // Not interactive in review overlay
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview
@Composable
fun WordReviewPagePreview() {
    MaterialTheme {
        WordReviewPage(
            wordItem = sampleWordItem.copy(
                sentences = listOf("First sentence.", "Second sentence."),
                relatedWords = listOf("Related A", "Related B")
            ),
            onAction = {},
            onBack = {}
        )
    }
}
