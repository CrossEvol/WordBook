package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use auto-mirrored icon
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.model.WordItemUI
import com.crossevol.wordbook.ui.components.sampleWordItem // For preview
import io.github.oshai.kotlinlogging.KotlinLogging // Import KotlinLogging
import org.jetbrains.compose.ui.tooling.preview.Preview

private val logger = KotlinLogging.logger {} // Add logger instance

/**
 * Displays the details of a single WordItem.
 * Based on the design image provided (design/pages/WordDetail.png).
 *
 * @param wordItem The WordItem to display.
 * @param onBack Callback invoked when the back navigation is requested.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailPage(
    wordItem: WordItemUI,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Title can be empty or set dynamically if needed */ },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Match background potentially
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Add horizontal padding for content
        ) {
            // Word Name (Title)
            Text(
                text = wordItem.title,
                style = MaterialTheme.typography.headlineLarge, // Larger headline
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Pronunciation (like "[ Hello, world ]")
            Text(
                text = wordItem.pronunciation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Explanation Label
            Text(
                text = "Explanation", // "explanation" in design, capitalized here
                style = MaterialTheme.typography.titleSmall, // Smaller title for label
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Explanation Text
            Text(
                text = wordItem.explanation,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Sentences Label
            Text(
                text = "Sentences",
                style = MaterialTheme.typography.titleMedium, // Slightly larger title for section
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Sentences List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                wordItem.sentences.forEach { sentence ->
                    SentenceItem(text = sentence) {
                        // Handle sentence click if needed in the future
                        logger.debug { "Sentence clicked: $sentence" } // Replaced println
                    }
                }
            }
        }
    }
}

/**
 * A styled item for displaying a sentence, matching the design.
 */
@Composable
fun SentenceItem(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp), // Rounded corners
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) // Light background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No shadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Push arrow to the end
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f) // Allow text to take space
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View sentence detail", // Accessibility
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview
@Composable
fun WordDetailPagePreview() {
    MaterialTheme {
        Surface {
            WordDetailPage(
                wordItem = sampleWordItem.copy(
                    sentences = listOf(
                        "First sentence example.",
                        "Second sentence which might be a bit longer to see how it wraps."
                    )
                ),
                onBack = {}
            )
        }
    }
}
