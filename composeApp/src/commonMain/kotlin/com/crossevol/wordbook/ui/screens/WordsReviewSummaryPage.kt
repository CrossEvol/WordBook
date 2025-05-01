package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossevol.wordbook.data.model.WordItemUI
import com.crossevol.wordbook.ui.viewmodel.WordReviewViewModel
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailSummaryPage(
    viewModel: WordReviewViewModel,
    onStart: (List<WordItemUI>) -> Unit,
    onBack: () -> Unit,
    isPreview: Boolean = false // Flag for preview mode
) {
    // Collect state from the ViewModel
    val reviewState by viewModel.reviewState.collectAsState()

    // Load words when the screen is created
    LaunchedEffect(viewModel) {
        if (!isPreview) {
            viewModel.loadWordsForReview()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when (reviewState) {
            is WordReviewViewModel.ReviewState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is WordReviewViewModel.ReviewState.Error   -> {
                val errorMessage = (reviewState as WordReviewViewModel.ReviewState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No words to review",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadWordsForReview() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is WordReviewViewModel.ReviewState.Success -> {
                val words = (reviewState as WordReviewViewModel.ReviewState.Success).words
                ReviewContent(
                    words = words,
                    onStart = onStart,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ReviewContent(
    words: List<WordItemUI>,
    onStart: (List<WordItemUI>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // --- Fixed Top Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 20.dp)
                ) {
                    Text(
                        text = "Left",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = words.size.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = if (words.size == 1) " word" else " words",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            logger.info { "Start button pressed." }
                            onStart(words)
                        },
                        enabled = words.isNotEmpty(),
                        modifier = Modifier.width(320.dp).height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Start!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 28.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // --- Divider ---
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // --- Scrollable List Section ---
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(words) { word ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = word.title,
                                style = MaterialTheme.typography.headlineLarge,
                                maxLines = 1
                            )
                        }
                        HorizontalDivider(
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}
