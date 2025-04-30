package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crossevol.wordbook.data.model.WordItemUI // Assuming you might want to pass actual words later
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.ui.tooling.preview.Preview

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailSummaryPage(
    wordsToReview: List<WordItemUI>, // Pass the list of words to review
    onStart: () -> Unit,
    onBack: () -> Unit
) {
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
                    containerColor = MaterialTheme.colorScheme.primary, // Purple color
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
        ) {
            // --- Fixed Top Section ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ), // Add some padding around the card
                shape = MaterialTheme.shapes.medium, // Use standard shape
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // No elevation as per design
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Use surface color
            ) {
                Column(
                    modifier = Modifier.padding(12.dp), // Padding inside the card
                    horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, // Align text baselines or bottoms
                        horizontalArrangement = Arrangement.Center, // Center the row content
                        modifier = Modifier.padding(vertical = 20.dp) // Apply padding to the row
                    ) {
                        Text(
                            text = "Left",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ), // Adjust style as needed
                            modifier = Modifier.padding(end = 8.dp) // Space after "Left"
                        )
                        Text(
                            text = wordsToReview.size.toString(),
                            style = MaterialTheme.typography.displayLarge.copy( // Larger font size
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary // Appealing color
                            ),
                            modifier = Modifier.padding(bottom = 4.dp) // Fine-tune alignment if needed
                        )
                        Text(
                            text = if (wordsToReview.size == 1) " word" else " words", // Handle singular/plural
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ), // Adjust style as needed
                            modifier = Modifier.padding(start = 8.dp) // Space before "word(s)"
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly // Space buttons evenly
                    ) {
                        Button(
                            onClick = {
                                logger.info { "Start button pressed." }
                                onStart()
                            },
                            modifier = Modifier.width(320.dp).height(56.dp),
                            shape = MaterialTheme.shapes.medium, // Standard button shape
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary // Purple button
                            )
                        ) {
                            Text(
                                "Start!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 28.sp, // Match font size from Flutter code
                                    color = MaterialTheme.colorScheme.onPrimary // White text
                                )
                            )
                        }
                    }
                }

                // --- Divider ---
                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant // Use a subtle divider color
                )

                // --- Scrollable List Section ---
                LazyColumn(
                    modifier = Modifier.weight(1f) // Takes remaining space
                ) {
                    items(wordsToReview) { word ->
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp) // Fixed height for list items
                                    .padding(horizontal = 20.dp), // Padding for text
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = word.title, // Display word title
                                    style = MaterialTheme.typography.headlineLarge,
                                    maxLines = 1 // Ensure text stays on one line if too long
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
}

// --- Preview ---
@Preview
@Composable
fun WordDetailSummaryPagePreview() {
    // Create some dummy data for the preview
    val sampleWords = List(5) { index ->
        WordItemUI(
            id = index.toLong(),
            title = "Hello World $index",
            pronunciation = "həˈloʊ wɜːld",
            explanation = "A common greeting.",
            rating = 3L,
            sentences = listOf("The program prints 'Hello World'."),
            relatedWords = listOf(
                "Greeting",
                "Introduction"
            )
        )
    }
    MaterialTheme { // Wrap preview in MaterialTheme
        WordDetailSummaryPage(
            wordsToReview = sampleWords,
            onStart = { println("Preview Start clicked") },
            onBack = { println("Preview Back clicked") }
        )
    }
}
