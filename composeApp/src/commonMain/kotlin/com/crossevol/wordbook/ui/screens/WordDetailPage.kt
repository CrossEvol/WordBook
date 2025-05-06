package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.mock.sampleWordItem
import com.crossevol.wordbook.data.model.WordItemUI
import com.crossevol.wordbook.ui.components.WordDetailsContent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.ui.tooling.preview.Preview

private val logger = KotlinLogging.logger {} // Add logger instance

/**
 * Displays the details of a single WordItem.
 * Based on the design image provided (design/pages/WordDetail.png).
 *
 * @param wordItem The WordItem to display.
 * @param onBack Callback invoked when the back navigation is requested.
 */
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
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
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
