package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crossevol.wordbook.data.mock.sampleWordItem
import com.crossevol.wordbook.data.model.WordItemUI
import com.crossevol.wordbook.ui.components.RelatedWordItem
import com.crossevol.wordbook.ui.components.SentenceItem
import com.crossevol.wordbook.ui.components.SimpleActionSheet
import com.crossevol.wordbook.ui.components.WordDetailsContent
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A screen for reviewing a word, showing details with a blurred overlay and action sheet.
 * When user chooses Remember/Forget/Skip, it shows the intermediate state first before proceeding.
 *
 * @param wordItem The WordItem to display for review.
 * @param remainingWordsCount The count of remaining words to display in the title.
 * @param onRemember Callback invoked when the user chooses to remember the word.
 * @param onForget Callback invoked when the user chooses to forget the word.
 * @param onSkip Callback invoked when the user chooses to skip the review.
 * @param onBack Callback invoked when the back navigation is requested.
 * @param onNext Callback invoked when the user taps Next in the answer state.
 */
@Composable
fun WordReviewPage(
    wordItem: WordItemUI,
    remainingWordsCount: Int,
    onRemember: () -> Unit,
    onForget: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit = {}
) {
    var reviewState by remember { mutableStateOf(ReviewState.QUESTION) }
    var reviewAction by remember { mutableStateOf<ReviewAction?>(null) }

    when (reviewState) {
        ReviewState.QUESTION -> {
            QuestionReviewScreen(
                wordItem = wordItem,
                remainingWordsCount = remainingWordsCount,
                onRemember = {
                    reviewAction = ReviewAction.REMEMBER
                    reviewState = ReviewState.ANSWER
                    onRemember()
                },
                onForget = {
                    reviewAction = ReviewAction.FORGET
                    reviewState = ReviewState.ANSWER
                    onForget()
                },
                onSkip = {
                    reviewAction = ReviewAction.SKIP
                    reviewState = ReviewState.ANSWER
                    onSkip()
                },
                onBack = onBack
            )
        }

        ReviewState.ANSWER   -> {
            AnswerReviewScreen(
                wordItem = wordItem,
                remainingWordsCount = remainingWordsCount,
                onNext = {
                    // Execute the action to move to next word
                    onNext()
                    // Reset state for next word
                    reviewState = ReviewState.QUESTION
                },
                onBack = onBack
            )
        }
    }
}

/**
 * Represents the state of the review screen
 */
private enum class ReviewState {
    QUESTION, // Showing the question (word with blurred details)
    ANSWER    // Showing the answer (word with full details)
}

/**
 * Represents the action chosen by the user
 */
private enum class ReviewAction {
    REMEMBER,
    FORGET,
    SKIP
}

/**
 * The initial question screen with the word title and blurred content
 */
@Composable
private fun QuestionReviewScreen(
    wordItem: WordItemUI,
    remainingWordsCount: Int,
    onRemember: () -> Unit,
    onForget: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Content with blur effect
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Left $remainingWordsCount") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        backgroundColor = MaterialTheme.colors.surface,
                        contentColor = MaterialTheme.colors.onSurface,
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    // Unblurred title
                    Text(
                        text = wordItem.title,
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Blurred content (everything except the title)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .blur(8.dp)
                    ) {
                        Column {
                            // Pronunciation
                            Text(
                                text = wordItem.pronunciation,
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Rest of the word details
                            WordDetailsContentWithoutTitle(
                                wordItem = wordItem,
                                onSentenceClick = {},
                                onRelatedWordClick = {}
                            )
                        }
                    }
                }
            }
        }

        // 2. Semi-Transparent Overlay - exclude the title area
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp)
                .clickable(
                    enabled = false,
                    onClick = {}),
            color = Color.Black.copy(alpha = 0.5f)
        ) {}

        // 3. Action Sheet at the Bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            SimpleActionSheet(
                onEdit = onRemember,
                onDelete = onForget,
                onCancel = onSkip,
                editLabel = "Remember",
                deleteLabel = "Forget",
                cancelLabel = "Skip",
                modifier = Modifier.padding(
                    bottom = 24.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            )
        }
    }
}

/**
 * The answer screen showing full word details with a Next FAB
 */
@Composable
private fun AnswerReviewScreen(
    wordItem: WordItemUI,
    remainingWordsCount: Int,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Left $remainingWordsCount") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.onSurface,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNext,
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next word"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Full word details without blur
            WordDetailsContent(
                wordItem = wordItem,
                onSentenceClick = {},
                onRelatedWordClick = {}
            )
        }
    }
}

/**
 * Displays WordDetailsContent without the title, for use with separate unblurred title
 */
@Composable
private fun WordDetailsContentWithoutTitle(
    wordItem: WordItemUI,
    modifier: Modifier = Modifier,
    onSentenceClick: (String) -> Unit = {},
    onRelatedWordClick: (String) -> Unit = {}
) {
    Column(modifier = modifier) {
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
            text = "Explanation",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Explanation Text
        Text(
            text = wordItem.explanation,
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Sentences Label
        Text(
            text = "Sentences",
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Sentences List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            wordItem.sentences.forEach { sentence ->
                SentenceItem(text = sentence) {
                    onSentenceClick(sentence)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Related Words Label
        Text(
            text = "Related Words",
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Related Words List
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            wordItem.relatedWords.forEach { relatedWord ->
                RelatedWordItem(text = relatedWord) {
                    onRelatedWordClick(relatedWord)
                }
            }
        }
    }
}

@Preview
@Composable
fun WordReviewPagePreview() {
    MaterialTheme {
        WordReviewPage(
            wordItem = sampleWordItem.copy(
                sentences = listOf(
                    "First sentence example for review.",
                    "Second sentence example for review."
                ),
                relatedWords = listOf(
                    "Related Word A",
                    "Related Word B",
                    "Related Word C"
                )
            ),
            remainingWordsCount = 5,
            onRemember = {},
            onForget = {},
            onSkip = {},
            onBack = {}
        )
    }
}
