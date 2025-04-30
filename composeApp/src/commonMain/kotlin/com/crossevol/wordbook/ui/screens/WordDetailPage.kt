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
import com.crossevol.wordbook.data.mock.sampleWordItem
import com.crossevol.wordbook.data.model.WordItemUI
import com.crossevol.wordbook.ui.components.RelatedWordItem // Import RelatedWordItem
import com.crossevol.wordbook.ui.components.SentenceItem // Import SentenceItem
import com.crossevol.wordbook.ui.components.WordDetailsContent // Import the new component
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
        // Use the extracted WordDetailsContent component
        WordDetailsContent(
            wordItem = wordItem,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp), // Add horizontal padding for content
            onSentenceClick = { sentence ->
                // Handle sentence click if needed in the future
                logger.debug { "Sentence clicked: $sentence" }
            },
            onRelatedWordClick = { relatedWord ->
                // Handle related word click if needed
                logger.debug { "Related Word clicked: $relatedWord" }
            }
        )
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
                    ),
                    relatedWords = listOf(
                        "Related 1",
                        "Related 2",
                        "Related 3"
                    )
                ),
                onBack = {}
            )
        }
    }
}
